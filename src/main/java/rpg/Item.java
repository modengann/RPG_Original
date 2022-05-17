package rpg;
/**
 * Write a description of class Item here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Item extends VisibleObject {
    private int count;

    /**
     * Constructor for objects of class Item
     */
    public Item(Script script) {
        super(script);
        if(script instanceof Item) {
            count = ((Item)script).count;
        }
    }
    
    public void loadInformation(Tile tile) {
        super.loadInformation(tile);
        count = tile.getParameter("c");
    }
    
    public int getCount() {
        return count;
    }
    
    public int goldValue() {
        return getTile().getParameter("g");
    }
    
    public String getDescription() {
        if(count == 1) {
            return getName();
        } else {
            return getName() + " (" + count + ")";
        }
    }
    
    public int getSlot() {
        return getTile().getParameter("E");
    }
    
    public boolean canUse() {
        return getTile().getFlag("U");
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public boolean isRealItem() {
        return getTile().getNumber() != -1;
    }
       
    public void updateArgument() {
        Tile tile = getTile();
        String arg;
        if(tile.getNumber() != -1) {
            arg = ":t" + tile.getNumber();
            if(!getName().equals(tile.getName())) arg = getName() + arg;
            if(count != tile.getParameter("c")) arg += "c" + count;
        } else {
            tile.setParameter("c", count);
            arg = tile.toString();
        }
        setArgument(arg);
    }
}
