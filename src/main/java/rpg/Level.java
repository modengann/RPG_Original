package rpg;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.io.FileReader;

/**
 * A <code>Level</code> represents a single section of a town, dungeon, or
 * the outdoors in Project RPG.
 * 
 * In part 2 of this project, you will make <code>Level</code> able to maintain
 * a list of all the <code>Script</code>s in it. This will require you to be
 * able to add and remove from that list, and to search through your list for
 * scripts that match a certain template. A Level is an interesting sort of list
 * because there is no way simply to view all the scripts in it; instead, you
 * must always provide it with a template for the sort of scripts you are
 * interested in seeing.
 * 
 * In part 3 of this project, you will make <code>Level</code> load itself from
 * a file. This will require you to write a "factory" method in the
 * <code>Script</code> class that will construct a new <code>Script</code> from
 * a single <code>String</code>. It will also require you to load the information
 * on the dimensions of the level, and the hexadecimal encoding of the tiles,
 * from that same file. In this section you will add some instance variables and
 * their getters.
 * 
 * @author (your name here)
 * @version (date)
 */
public class Level extends Object {

/* * * * * You will need to add instance variables for parts 2 and 3 * * * * */

    private String name;
    private int width, height;
    
    /**
     * Return the name of this level.
     * @return The name of this level.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Return the width of this level.
     * @return The width of this level.
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Return the height of this level.
     * @return The height of this level.
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * You can use this method to load a single level from the file "test.txt".
     * This gives you a way to check that file loading is working without running
     * the full program.
     * @return a newly loaded Level
     */
    public static Level testLoadFullLevel() {
        try {
            Tile.loadTiles();
            BufferedReader reader = new BufferedReader(new FileReader(new File("./test.txt")));
            return new Level(reader);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * You can use this method to test out your <code>loadTiles()</code> method
     * by just creating an empty level with the Level() constructor and then
     * telling it to <code>loadTiles()</code>.
     * @return a newly loaded Level
     */
    public static Level testLoadTiles() {
        try {
            Level level = new Level();
            Tile.loadTiles();
            BufferedReader reader = new BufferedReader(new FileReader(new File("./test.txt")));
            reader.readLine();
            reader.readLine();
            level.width = 8;
            level.height = 4;
            level.loadTiles(reader);
            return level;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

/* * * * * Beginning of part 2 * * * * */

    /**
     * Construct a new Level that has no scripts in it, but is ready
     * to start adding scripts.
     */
    public Level() {
    }
    
    /**
     * Add the given script to this level.
     * @param script Script to add
     */
    public void addScript(Script script) {
    }
    
    /**
     * Remove the given script from the level.
     * @param script script to remove
     */
    public void removeScript(Script script) {
    }
    
    /**
     * Return an <code>ArrayList</code> containing all scripts in this
     * <code>Level</code> that match the given script.
     * 
     * @param template template to match to
     * @return An <code>ArrayList</code> of all matching scripts.
     */
    public ArrayList getScripts(Script template) {
        return null;
    }
    
    /**
     * Return the first script in this <code>Level</code> that matches the
     * given template, or <code>null</code> if there are no matching scripts.
     * 
     * @param template template to match to
     * @return The first matching script, or <code>null</code> if there are
     *          no matching scripts in this <code>Level</code>.
     */
    public Script getScript(Script template) {
        return null;
    }
    
    /**
     * Return the <code>n</code>th script in this <code>Level</code>
     * that matches the given template, or <code>null</code> if there
     * are not that many matching scripts.
     * 
     * @param n index of script to find. Index 0 is the first script that
     *          matches the template, and so on.
     * @param template template to match to
     * @return The <code>n</code>th matching script, or <code>null</code>
     *          if there are <code>n</code> or fewer matching scripts in
     *          this <code>Level</code>.
     */
    public Script getScript(int n, Script template) {
        return null;
    }
    
/* * * * * Beginning of part 3 * * * * */
    
    /**
     * Load just the information about the tiles of this level from a file.
     * The BufferedReader you are given will give you one line at a time of
     * the tile data if you call its readLine() method; it should start at
     * the start of the tile data and you should leave it at the end of the
     * tile data.
     * @param file BufferedReader to read from, representing the file.
     */
    public void loadTiles(BufferedReader file) throws IOException {
    }
    
    /**
     * Return a <code>Tile</code> object representing the tile at the given
     * (x, y) location. If the given location is outside of the level, return
     * null.
     * @param x x coordinate (from 0 to <code>width</code> - 1)
     * @param y y coordinate (from 0 to <code>height</code> - 1)
     * @return the tile at the given (x, y) location
     */
    public Tile getTile(int x, int y) {
    	return null;
    }

    /**
     * Place a new tile at the given (x, y) location. If that location is
     * outside the level, do nothing.
     * @param x x coordinate (from 0 to <code>width</code> - 1)
     * @param y y coordinate (from 0 to <code>height</code> - 1)
     * @param tile Tile to place at that location.
     * @return the tile at the given (x, y) location
     */
    public void setTile(int x, int y, Tile tile) {
    }

    /**
     * Construct a new Level by reading all its parameters from the given
     * <code>file</code>. A <code>BufferedReader</code> will allow you to 
     * get the lines of the file as <code>String</code>s one by one by
     * calling its <code>readLine()</code> method.
     * @param file file to load from
     */
        // The "throws" below just warns the computer that if something
        // bizarre happens there might be an exception produced as we try
        // to read the file.
    public Level(BufferedReader file) throws IOException {
    }
    
/* * * * * Extension for part 3 * * * * */
    
    /**
     * Save this level into the given FileWriter. You can write a line of text
     * to the file with <code>writer.write(line + "\n")</code>. This method
     * should exactly reverse the process you used to load the level.
     * 
     * Part of doing this is to fill in the |toString()| method of |Script|
     * so that it creates a string liek that you create scripts from, the x, 
     * y, action, and argument with single spaces between them.
     */
    public void writeToFile(FileWriter file) throws IOException {
    }
}
