package rpg;

import java.util.HashMap;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.File;
import java.util.Iterator;

/**
 * A Map represents the entire world of an RPG. It is basically just a
 * collection of levels, which can be retrieved by name; it also knows
 * which Creature is currently controlled by the player and is able to
 * return that creature.
 * 
 * @author Russell Zahniser 
 * @version 2006-02-15
 */
public class Map extends Object {
    private static Map map;
    private static KeyInterpreter key = null;
    
    private RPGView view;
    private HashMap levels;
    private Creature player;

    /**
     * Create a new Map, reading from the given file. Attach this map to 
     * the given applet.
     * @param file File to read from
     * @param applet RPGApplet that will be representing this map. This
     * can be null if you are just loading the map for debugging reasons.
     */
    public Map(BufferedReader file, RPGView view) throws IOException {
        super();
        levels = new HashMap();
        
        map = this;
        
        this.view = view;
        if(view != null && key == null) {
            key = new KeyInterpreter();
            view.addKeyListener(key);
        }
        
        String start = file.readLine();
        
        while(file.readLine() != null) {
            Level level = new Level(file);
            levels.put(level.getName(), level);
        }
        
        Location startLocation = getLocation(start);
        
        setPlayer((Creature)startLocation.getLevel().getScript(new Script(
            startLocation.getX(), startLocation.getY(), "Creature", null)));
    }
    
    /**
     * Get the Level in the map that has the given name.
     * @param name the name of the level you are looking for
     * @return the Level with that name, or null if no level has exactly that name.
     */
    public Level getLevel(String name) {
        if(levels.containsKey(name)) {
            return (Level)levels.get(name);
        } else {
            return null;
        }
    }
    
    /**
     * Add a level to this map
     * @param level new level to add
     */
    public void addLevel(Level level) {
        levels.put(level.getName(), level);
    }
    
    /**
     * Ask this map to parse a location in the form "X Y Level Name" and return
     * a Location object representing that location. The Location object simply
     * has x, y, and level locations.
     * @param location String to parse, in the format "X Y Level Name"
     * @return a Location object with the x, y, and level coordinates
     */
    public Location getLocation(String location) {
        int i = location.indexOf(" "), x, y;
        x = Integer.parseInt(location.substring(0, i));
        location = location.substring(i + 1);
        
        i = location.indexOf(" ");
        if(i == -1) {
            y = Integer.parseInt(location);
            return new Location(x, y, null);
        } else {
            y = Integer.parseInt(location.substring(0, i));
            location = location.substring(i + 1);
            if(!levels.containsKey(location)) {
                return new Location(x, y, null);
            } else {
                return new Location(x, y, (Level)levels.get(location));
            }
        }
    }
    
    /**
     * Get the Creature that is currently controlled by the player.
     * @return the Creature that the player controls
     */
    public Creature getPlayer() {
        return player;
    }
    
    /**
     * Return the KeyInterpreter used by objects in this map.
     * @return the KeyInterpreter for this map.
     */
    public KeyInterpreter getKey() {
        return key;
    }
    
    /**
     * Set which Creature is controlled by the player.
     * @param player Creature to become the player
     */
    public void setPlayer(Creature player) {
        this.player = player;
        if(key != null) key.setTarget(player);
    }
    
    /**
     * Make this map the active map. Usually you have only one map open at
     * a time, so you won't need this method.
     */
    public void makeActive() {
        Map.map = this;
    }
    
    /**
     * Return the Map that is currently active.
     * @return the active map
     */
    public static Map getMap() {
        return map;
    }
    
    /**
     * Call this method when a change has been made that should cause the
     * applet to redraw itself.
     */
    public void update() {
        if(view != null) view.repaint();
    }
    
    /**
     * Save this map to the given file
     * @param file File to save into
     */
    public void save(File file) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(player.getX() + " " + player.getY() + " " + player.getLevel().getName() + "\n");
            for(Iterator iter = levels.keySet().iterator(); iter.hasNext(); ) {
                Level level = (Level)levels.get(iter.next());
                writer.write("\n");
                level.writeToFile(writer);
            }
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
