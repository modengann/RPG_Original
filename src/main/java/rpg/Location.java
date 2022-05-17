package rpg;
/**
 * Write a description of class Location here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Location{
    private int x, y;
    private Level level;

    /**
     * Construct a new location at the given coordinates in the
     * given level.
     */
    public Location(int x, int y, Level level) {
        this.x = x;
        this.y = y;
        this.level = level;
    }
    
    /**
     * Get the x coordinate of this location
     * @return the x coordinate of this location
     */
    public int getX() {
        return x;
    }
    
    /**
     * Get the y coordinate of this location
     * @return the y coordinate of this location
     */
    public int getY() {
        return y;
    }
       
    /**
     * Get the Level this location is in
     * @return the Level this location is in
     */
    public Level getLevel() {
        return level;
    }
}
