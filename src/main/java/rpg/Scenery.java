package rpg;
/**
 * Write a description of class Scenery here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Scenery extends VisibleObject {
    /**
     * Constructor for objects of class Item
     */
    public Scenery(Script script) {
        super(script);
    }
    
    public void loadInformation(Tile tile) {
        super.loadInformation(tile);
    }
    
    public void action() {
        if(getTile().canAction()) {
            setTile(getTile().getBecome());
        }
        if(getContainer() != null && getTile().getFlag("T") && !getContainer().getItems().isEmpty()) {
            new Script(0, 0, "ContainerPanel", "Scenery").move(this);
        }
    }
    
    public void lever() {
        if(getTile().canLever()) {
            setTile(getTile().getBecome());
        }
    }
       
    public void updateArgument() {
        Tile tile = getTile();
        String arg;
        if(tile.getNumber() != -1) {
            arg = ":t" + tile.getNumber();
            if(!getName().equals(tile.getName())) arg = getName() + arg;
        } else {
            arg = tile.toString();
        }
        setArgument(arg);
    }
}
