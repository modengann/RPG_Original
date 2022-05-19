package rpg;
import java.util.ArrayList;

/**
 * Write a description of class VisibleObject here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class VisibleObject extends Script {
    private Tile tile;
    private ItemContainer container;
    private String name;
    
    /**
     * Create a new VisibleObject based on the given script.
     * @param script Script to convert into a VisibleObject
     */
    public VisibleObject(Script script) {
        super(script);
        
        tile = new Tile(getArgument(), -1);
        loadInformation(tile);
        
        if(tile.getParent() != null) tile = tile.getParent();
    }
    
    /**
     * Return this object's Tile
     * @return this object's Tile
     */
    public Tile getTile() {
        return tile;
    }
    
    public void setTile(Tile tile) {
        this.tile = tile;
        loadInformation(tile);
        
        if(tile.getParent() != null) tile = tile.getParent();
        
        updateArgument();
    }
    
    /**
     * Return the name of this object
     */
    public String getName() {
        return name;
    }
    
    /**
     * This method is called by the constructor to load all
     * the necessary instance variables from a <code>Tile</code>
     * object. You can ask for the value of a parameter (<code>int</code>
     * value) with <code>tile.getParameter("a")</code> or ask for a 
     * flag (<code>boolean</code> value) with <code>tile.getFlag("A")</code>.
     * Typically flags are uppercase letters and parameters are lowercase.
     * @param tile Tile containing parameters to load
     */
    public void loadInformation(Tile tile) {
        if(tile.getFlag("C")) {
            if(container == null) container = new ItemContainer();
        } else {
            container = null;
        }
        name = tile.getName();
    }
    
    /**
     * Return this object's item container, or null if it is not a container.
     * @return this object's item container
     */
    public ItemContainer getContainer() {
        return container;
    }
    
    /**
     * The argument of an object's script must be kept synchronized with its internally
     * held information if it is to be saved properly. This method rewrites the argument
     * based on the instance variables of the object. Call it before writing an object to
     * file with toString().
     */
    public void updateArgument() {
        // Nothing to update, for generic VisibleObject
    }
    
    /**
     * Call this method when you have made a change that should appear in the
     * view. This will just make the applet repaint itself.
     */
    public void update() {
        Map.getMap().update();
    }
    
    /**
     * This method will have no effect unless you have an ItemContainer. If
     * you do, it will drop the given item under this object.
     * @param item name of item to drop
     * @param count number of that item to drop
     */
    public void dropItem(String item, int count) {
        if(container == null) return;
        Item i = container.removeItem(item, count);
        i.move(this);
    }
    
    public String toString() {
        updateArgument();
        String description = super.toString();
        if(container != null) {
            ArrayList items = container.getItems();
            for(int i = 0; i < items.size(); i++) {
                Item item = (Item)items.get(i);
                item.move(getX(), getY());
                description += "\n" + item;
            }
        }
        return description;
    }
}
