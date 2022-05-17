package rpg;
import java.util.ArrayList;

/**
 * An ItemContainer contains a list of items that can be owned by any Creature
 * that has a conversation or any Item or Scenery that has the Container flag.
 * It is possible to add and remove items from a container, or to get a whole
 * list of everything in the container.
 * 
 * This class is the extension for part 2. It works in a similar way to Level.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */

public class ItemContainer {
    
    /**
     * Create a new, empty ItemContainer that is ready to start having
     * items added to it.
     */
    public ItemContainer() {
    }
    
    /**
     * Return an ArrayList containing all the Items in this Container.
     * @return an ArrayList containing Item objects
     */
    public ArrayList getItems() {
    	return null;
    }
    
    /**
     * Get the Item in this container that has the given name. Return null
     * if no item in the container has that exact name.
     * @param name name of Item to look for
     * @return item with the requested name, or null if none was found
     */
    public Item getItem(String name) {
        return null;
    }
    
    /**
     * Add the given item to this container. If it already has an item with the
     * same name, this method will add the counts of the two items, rather than
     * adding another item at the end of the list.
     * @param item Item to add
     */
    public void addItem(Item newItem) {
    }
    
    /**
     * Remove the requested number of items with the given name from this
     * container.
     * 
     * If the count requested is more than the number in the
     * container, a new Item object will be created and returned, and the
     * container will still have some left in it. (You can create a new
     * item that is the duplicate of another with new Item(item))
     * 
     * If the count is equal to the number of that item present, or if the
     * count is -1, all the items of that name are returned.
     * 
     * Otherwise, if count is greater than the number of that item present,
     * nothing is removed and null is returned.
     * 
     * @param name Name of item to remove
     * @param count Number of that item to remove, or -1 to remove all
     * @return the item removed, or null if not enough of it were present
     */
    public Item removeItem(String name, int count) {
        return null;
    }
}
