package rpg;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.applet.Applet;
import java.applet.AppletStub;
import java.applet.AppletContext;
import java.applet.AudioClip;
import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.HashMap;
import java.awt.Toolkit;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * TurtlesApp is the main class of this program. It makes a new window that
 * contains the Turtles applet. It can also display just the canvas in a new
 * window.
 * 
 * To run the application, run the method main().
 * 
 * @author Russell Zahniser
 * @version 5/23/05
 */
public class RPGApp extends JFrame implements AppletStub, AppletContext {
    private static RPGApp app;
    
    private class AppletEnumerator implements Enumeration {
        private boolean next = true;
        
        public Object nextElement() {
            if(next) {
                return applet;
            } else {
                return null;
            }
        }
        
        public boolean hasMoreElements() {
            return next;
        }
    }
    
	
	private static JFileChooser chooser = new JFileChooser(System.getProperty("user.dir") + "/rpgMaps");
	private static FileFilter txtFilter = new FileFilter(){
		public boolean accept(File f)
		{return f.isDirectory() || f.getName().endsWith(".txt");}
		
		public String getDescription() {return "Text file";}
	};
	
    private RPGApplet applet = new RPGApplet();
    private boolean active = false;
    private HashMap streams = new HashMap();
    
    public static RPGApp getApp() {
        return app;
    }
    
    public void save() {
		if(chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
			Map.getMap().save(chooser.getSelectedFile());
    }
    
    public void open() {
		if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
		    try {
		        applet.loadMap(new FileInputStream(chooser.getSelectedFile()));
		    } catch(Exception e) {
		        e.printStackTrace();
		    }
		}
    }

    private RPGApp() {
        super("RPG");
        
        app = this;
        chooser.setFileFilter(txtFilter);
         
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().add(applet, BorderLayout.CENTER);
        setResizable(false);
        setVisible(true);
            
        // tell the applet to lay itself out
        validate();

        // set up actions to take when window closes
        addWindowListener(new WindowAdapter()
        {
            public void windowClosed (WindowEvent e)
            {
                // shut down the applet
                active = false;
                applet.stop();
                applet.destroy();
            }
        });
        
        applet.setStub(this);
        applet.init();
        active = true;
        applet.start();
        
        validate();
    }
    
    public boolean isActive() {
        return active;
    }
     
    /**
     * This is here to help out the applet. You don't need to use it.
     */
    public void appletResize(int width, int height)
    {
        setSize(width,height);
    }
    
    /**
     * This is here to help out the applet. You don't need to use it.
     */
    public AppletContext getAppletContext()
    {
        return this;
    }
    
    /**
     * This is here to help out the applet. You don't need to use it.
     */
    public String getParameter(String name)
    {
        return null;
    }
    
    /**
     * This is here to help out the applet. You don't need to use it.
     */
    public URL getCodeBase()
    {
        try {
            return new File(".").toURI().toURL();
        } catch (java.net.MalformedURLException mue) {
            return null;
        }
    }
    
    /**
     * This is here to help out the applet. You don't need to use it.
     */
    public URL getDocumentBase()
    {
        return getCodeBase();
    }
    
    /**
     * Run the applet in a new window.
     */
    public static void main(String[] args) {
        new RPGApp();
    }
    
    /**
     * Return the applet
     */
    public Applet getApplet(String name) {
        return applet;
    }
    
    /**
     * Return an enumeration containing the applet
     */
    public Enumeration getApplets() {
        return new AppletEnumerator();
    }
        
    /**
     * Return an audio clip from the given URL
     */
    public AudioClip getAudioClip(URL url) {
        return null;
    }
    
    /**
     * Return an image from the given URL
     */
    public Image getImage(URL url) {
        return Toolkit.getDefaultToolkit().getImage(url);
    }
    
    /**
     * Return the input stream associated with the given key
     */
    public InputStream getStream(String key) {
        return (InputStream)streams.get(key);
    }
    
    /**
     * Return an iterator over all the stream keys
     */
    public Iterator getStreamKeys() {
        return streams.keySet().iterator();
    }
    
    /**
     * Associate the given stream with the given key
     */
    public void setStream(String key, InputStream stream) {
        streams.put(key, stream);
    }
    
    public void showDocument(URL url) {
    }
    
    public void showDocument(URL url, String target) {
    }
    
    public void showStatus(String status) {
    }
 }
