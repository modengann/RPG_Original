package rpg;

import java.util.HashMap;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * The KeyInterpreter is given a target object to forward key events to.
 * It receives key events from the applet, and calls the target's method
 * key___Pressed or key___Released where ___ is the name of the key pressed.
 */
public class KeyInterpreter implements KeyListener {
    //  Map of code to name
    private static HashMap codeNames;
    //  Map of name to code
    private static HashMap keyCodes = buildKeyCodes();
    //  object to forward events to
    private Object target = null;
    // Keys currently held down
    private static HashSet keysDown = new HashSet();
    // Stack of previous targets
    private ArrayList targets = new ArrayList();
    
    private static final boolean isMac = System.getProperty("os.name").indexOf("Mac") != -1;
    
    /**
     * Give key focus to the given object.
     * @param target New target of this KeyInterpreter.
     */
    public void setTarget(Object target) {
        for(Iterator iter = keysDown.iterator(); iter.hasNext(); ) {
            doKey("Released", (Integer)iter.next());
        }
        this.target = target;
        for(Iterator iter = keysDown.iterator(); iter.hasNext(); ) {
            doKey("Pressed", (Integer)iter.next());
        }
    }
    
    /**
     * Change the target, but remember the old target so that we can go
     * back to it. 
     * @param target New target to add
     */
    public void pushTarget(Object target) {
        targets.add(this.target);
        setTarget(target);
    }
    
    /**
     * Remove the given target, and everythign above it, from the list of
     * targets; target the next one below that. Has no effect if there are
     * no stored targets or the given object is not on the list of objects.
     * @param target Target to remove
     */
    public void popTarget(Object target) {
        if(this.target != target) {
            int i = targets.indexOf(target); 
            if(i == -1) return;
            while(i < targets.size()) targets.remove(i);
        }
        setTarget(targets.remove(targets.size() - 1));
    }
    
    /**
     * Return the target of this KeyInterpreter
     * @return The target object of this KeyInterpreter.
     */
    public Object getTarget() {
        return target;
    }
    
    /**
     * Is this object either the target of this KeyInterpreter or one of
     * the old targets it is holding?
     * @param target Object to look for
     * @return true if the given object is found anywhere amoung the current
     * or stored targets of this KeyInterpreter.
     */
    public boolean isTarget(Object target) {
        return this.target == target || targets.contains(target);
    }
    
    // The system will call this method when a key is pressed.
    private void keyEvent(KeyEvent event) {
        String type;
        Integer code = new Integer(event.getKeyCode());
        
        /*
         * Some systems will continue to produce key pressed events as long as the key
         * is down. We keep a list of what keys are down, and only call the "Down" method
         * when the key was really just pressed down. Otherwise, we call the "Held" method.
         * The "Held" method should probably not be implemented: you can't trust that all
         * systems will keep giving key pressed events.
         */
        if(event.getID() == KeyEvent.KEY_PRESSED) {
            if(keysDown.contains(code)) {
                type = "Held";
            } else {
                type = "Pressed";
                keysDown.add(code);
            }
        } else if(event.getID() == KeyEvent.KEY_RELEASED) {
            type = "Released";
            keysDown.remove(code);
        } else {
            return;
        }
        if(type.equals("Released") && RPGApp.getApp() != null &&
                ((isMac && event.isMetaDown()) || (!isMac && event.isControlDown()))) {
            if(event.getKeyCode() == KeyEvent.VK_S) {
                RPGApp.getApp().save();
            } else if(event.getKeyCode() == KeyEvent.VK_O) {
                RPGApp.getApp().open();
            }
        } else {
            if(doKey(type, code)) {
            	event.consume();
            } else {
				try {
					Class[] c = new Class[1];
					c[0] = String.class;
					String[] key = new String[1];
					if((event.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
						key[0] = capitalizedNameForKeyCode(code);
					} else {
						key[0] = nameForKeyCode(code);
					}
					if((event.getModifiers() & InputEvent.CTRL_MASK) != 0) {
						key[0] = "^" + key[0];
					}
					Method method = target.getClass().getMethod("otherKey" + type, c);
					method.invoke(target, key);
					event.consume();
				} catch(Exception f) {
				}
            }
        }
    }
    
    // Send a key event to the current keyFocus object
    private boolean doKey(String type, Integer code) {
        if(target == null) {
            return true;
        }

        String name = capitalizedNameForKeyCode(code);
        if(name == null) {
            return true;
        }
        
        String methodName = "key" + name + type;
        
        try {
            Method method = target.getClass().getMethod(methodName, null);
            method.invoke(target, null);
            return true;
        } catch(Exception e) {
        	return false;
        }
    }

    /*
     * In order to build our reference table, we parse the KeyEvent class
     * and extract the name and value of any field that begins with "VK_".
     */
    private static HashMap buildKeyCodes() {
        HashMap keyCodes = new HashMap();
        codeNames = new HashMap();
        try {
            Field[] fields = KeyEvent.class.getFields();
            for (int i = 0; i < fields.length; i++) {
                if ((fields[i].getModifiers() & Modifier.STATIC) != 0
                    && fields[i].getName().startsWith("VK_")) {
                    String name =
                        fields[i].getName().substring(3).toLowerCase();
                    Integer value = (Integer) fields[i].get(null);
                    keyCodes.put(name, value);
                    codeNames.put(value, name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyCodes;
    }

    private static Integer codeForKeyName(String key) {
        return (Integer) keyCodes.get(key.toLowerCase());
    }

    private static String nameForKeyCode(Integer code) {
        return (String) codeNames.get(code);
    }

    private static String uppercaseNameForKeyCode(Integer code) {
        return ((String) codeNames.get(code)).toUpperCase();
    }

    private static String capitalizedNameForKeyCode(Integer code) {
        String s = (String) codeNames.get(code);
        return s.substring(0, 1).toUpperCase().concat(s.substring(1));
    }
    /* * * Implementation of KeyListener * * */

    public void keyTyped(KeyEvent event) {
        // We don't deal with this type of event, only press and release.
    }
    
    public void keyPressed(KeyEvent event) {
        keyEvent(event);
    }

    public void keyReleased(KeyEvent event) {
        keyEvent(event);
    }
}