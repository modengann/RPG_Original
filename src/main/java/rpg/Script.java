package rpg;

/**
 * A <code>Script</code> is a generic class that can describe any of a number of objects that can be
 * part of a level. Creatures, scenery, and items are all scripts, but so are other things like notes
 * on where a tile leads into a town.
 * 
 * @author (your name here)
 * @version (date)
 */
public class Script {
/* * * * * You may need to add to or change this code in doing the later parts * * * * */

    private int x, y;
    private String action, argument;
    
    /**
     * Constructor a Script at the given location with the given action and argument.
     * @param x x location, or -1 to match any x
     * @param y y location, or -1 to match any y
     * @param action The action, or type, of the script. For example, "Town" or "Creature".
     *               Pass null to make a script that will match any action.
     * @param argument The argument of the script, giving the extra information on how it
     *               does its action.
     */
    public Script(int x, int y, String action, String argument) {
        super();
        this.x = x;
        this.y = y;
        this.action = action;
        this.argument = argument;
    }
    
    public Script(Script other) {
        this(other.x, other.y, other.action, other.argument);
    }
    
    /**
     * Return the x coordinate of this script.
     * @return the x coordinate of this script
     */
    public int getX() {
        return x;
    }
    
    /**
     * Return the y coordinate of this script.
     * @return the y coordinate of this script
     */
    public int getY() {
        return y;
    }
    
    /**
     * Return the action of this script.
     * @return the action of this script
     */
    public String getAction() {
        return action;
    }
    
    /**
     * Return the argument of this script.
     * @return the argument of this script
     */
    public String getArgument() {
        return argument;
    }
    
    /**
     * Move this script to a new location in the same level.
     * @param x new x location
     * @param y new y location
     */
    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Change the argument of this script
     * @param argument new value for the argument.
     */
    public void setArgument(String argument) {
        this.argument = argument;
    }
    
    public String toString() {
        return x + " " + y + " " + action + " " + argument;
    }
    
/* * * * * Beginning of part 1 * * * * */

    /**
     * Determine whether this script matches another script. This is more complicated than a simple
     * <code>equals()</code> method, because not all the instance variables will be used as criteria
     * for comparison; if either <code>this</code> or <code>other</code> has -1 as one of its
     * coordinates or <code>null</code> as its action or parameter, that property is automatically
     * assumed to match.
     * 
     * If you are doing the "radius" extension in part 1, you will have to write slightly more
     * complicated code in here to calculate the distance between the two scripts and verify that
     * it is less than the sum of their radii.
     * @param other other <code>Script</code> to compare to.
     * @return true if the two scripts match, false otherwise
     */
    public boolean matches(Script other) {
    	return false;
    }
    
/* * * * * Extension for part 1 * * * * */

    /**
     * Set this script to have an area effect. You can imagine it as a circle centered at
     * its given point. Any other script that intersects that circle will match it.
     * 
     * This is only used by an extension in part 1. Regular scripts may be considered to have
     * a radius of 0.
     * 
     * @param r new radius to set
     */
    public void setRadius(double r) {
    }
    
    /**
     * Get the radius over which this script has an effect. You only need to change this method
     * if you are doing the relevant extension in part 1; regular scripts are assumed to all have
     * a radius of 0.
     * 
     * @return the radius
     */
    public double getRadius() {
        return 0;
    }
    
/* * * * * Beginning of part 2 * * * * */
    
    /**
     * Return the Level that this script is in.
     * @return the Level that this script is in
     */
    public Level getLevel() {
        return null;
    }

    /**
     * If this script is in a level, <code>remove()</code> will remove it from the level.
     * Otherwise, there will be no effect.
     */
    public void remove() {
    }
    
    /**
     * Move this script to the given location in the given level. If it is already in that level,
     * only its location will be changed. In particular, this method will not change the position
     * of this script in the script list for its level if it is not, in fact, moving to a new level.
     * 
     * @param x x location to move to
     * @param y y location to move to
     * @param level level in which this script should end up. If <code>level</code> is null, the script
     *              is removed from its level.
     */
    public void move(int x, int y, Level level) {
    }
    
    /**
     * Move this script to the same location as the given script, in the same level as that script.
     * If it is already in that level, only its location will be changed. In particular, this method
     * will not change the position of this script in the script list for its level if it is not,
     * in fact, moving to a new level.
     * 
     * @param script Other script to whose location this one should move
     */
    public void move(Script other) {
    }

/* * * * * Beginning of part 3 * * * * */
    
    /**
     * This is a "factory" method, a static method whose job it is to instantiate objects of a
     * class. You will use methods like this when you aren't sure whether the object you return
     * will belong to this base class, or to one of its subclasses. So, for example, here if I
     * create a script with type "Creature" it will really instantiate a <code>Creature</code>
     * object and return it.
     * 
     * (This still agrees with the return type, because <code>Creature</code> is a subclass of
     * <code>Script</code>)
     * @param line String to parse to create the script
     * @return script that was created; possibly a subclass of <code>Script</code>.
     */
    public static Script createScript(String line) {
        return null;
    }
}
