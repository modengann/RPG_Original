package rpg;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * This class contains test cases testing the Script class. You will use more
 * of these methods as you progress in the project. When you make a change to
 * the Script class, it would be smart to re-run the test methods to make sure
 * everythign still works.
 *
 * @author  Russell Zahniser
 * @version 2006-02-09
 */
public class TestCases extends junit.framework.TestCase {

/* * * * * Beginning of part 1 * * * * */

    /**
     * This is a test for part 1. It will check that the matches() method works
     * as described on the website. It will also check to make sure that the 
     * constructor, getters, and setters are working properly.
     */
    public void testMatching() {
        Script fixedLocation = new Script(2, 3, "A", "B");
        Script differentObjects = new Script(2, 3, new String("A"), new String("B"));
        Script differentArgs = new Script(2, 3, "A", "A");
        Script differentAction = new Script(2, 3, "B", "B");
        Script differentX = new Script(3, 3, "A", "B");
        Script differentY = new Script(2, 4, "A", "B");
        Script anyY = new Script(2, -1, "A", "B");
        Script anyX = new Script(-1, 3, "A", "B");
        Script anyArg = new Script(4, 3, "A", null);
        Script anyAction = new Script(4, 3, null, "B");
        Script any = new Script(-1, -1, null, null);
        
        // Test constructor, getters, and setters
        // I already wrote these; this is to make sure you didn't do something
        // that would stop them from working.
        assertTrue("The getX() method should return the x location that was passed to the constructor.",
            fixedLocation.getX() == 2);
        assertTrue("The getY() method should return the y location that was passed to the constructor.",
            fixedLocation.getY() == 3);
        assertTrue("The getAction() method should return the action that was passed to the constructor.",
            ("A").equals(fixedLocation.getAction()));
        assertTrue("The getArgument() method should return the argument that was passed to the constructor.",
            ("B").equals(fixedLocation.getArgument()));
        
        try {
            // Test matches()
            assertTrue("A script should always match itself.",
                    fixedLocation.matches(fixedLocation));
            assertTrue("Even if my action and argument are different String objects, they should match if they contain the same value. Are you comparing them with == rather than .equals()?",
                    fixedLocation.matches(differentObjects));
            assertFalse("A script should not match a script with different argument.",
                fixedLocation.matches(differentArgs));
            assertFalse("A script should not match a script with different action.", 
                fixedLocation.matches(differentAction));
            assertFalse("A script should not match a script with different x.",
                fixedLocation.matches(differentX));
            assertFalse("A script should not match a script with different y.",
                fixedLocation.matches(differentY));
            assertTrue("An x of -1 should match any x.",
                fixedLocation.matches(anyX));
            assertTrue("A y of -1 should match any y.",
                fixedLocation.matches(anyY));
            assertTrue("If one script matches any x and another matches any y, and their arguments match, the two should match.",
                anyX.matches(anyY));
            assertTrue("If one script matches any action and another matches any argument, and their x and y match, the two should match.",
                anyArg.matches(anyAction));
            assertFalse("Even if null or -1 are present in other characteristics, any characteristic that fails (x, in this case) should mean that the two don't match.",
                anyY.matches(anyArg));
            assertTrue("A Script with (-1, -1, null, null) should match any script.",
                any.matches(fixedLocation) && any.matches(differentArgs) && any.matches(differentAction) && 
                any.matches(differentX) && any.matches(differentY) && any.matches(anyY) && any.matches(anyX) && 
                any.matches(anyArg) && any.matches(anyAction));
        } catch(NullPointerException e) {
            assertTrue("A NullPointerException was thrown during the testing of your matches() method. Make sure that if you ask your action and argument if they are .equals() to another, that you have checked first that the variable you are asking to do this is not null.", false);
            e.printStackTrace();
        } catch(Exception e) {
            assertTrue("A " + e.getClass().getName() + " was thrown during the testing of your matches() method.", false);
            e.printStackTrace();
        }
    }
    
    /**
     * This test is specifically for the "radius" extension in part 1. It will
     * verify that nearby scripts with nonzero radii can in fact overlap.
     */
    public void testRadius() {
        Script zero = new Script(2, 2, null, null);
        Script one = new Script(2, 3, null, null);
        one.setRadius(1);
        Script two = new Script(4, 5, null, null);
        two.setRadius(2);
        Script three = new Script(1, 0, null, null);
        three.setRadius(3);
        
        assertTrue("A Script that has had no radius set should have a radius of zero.", zero.getRadius() == 0);
        assertTrue("A Script at (0, 1) with radius 3 and a Script at (2, 2) with radius 0 should be close enough to touch.", three.matches(zero));
        assertTrue("A Script at (0, 1) with radius 3 and a Script at (2, 3) with radius 1 should be close enough to touch.", three.matches(one));
        assertFalse("A Script at (0, 1) with radius 3 and a Script at (4, 5) with radius 2 should not be close enough to touch.", three.matches(two));
        assertTrue("A Script at (2, 3) with radius 1 and a Script at (4, 5) with radius 2 should be close enough to touch.", one.matches(two));
        assertFalse("A Script at (2, 2) with radius 0 and a Script at (4, 5) with radius 2 should not be close enough to touch.", zero.matches(two));
        assertTrue("A Script at (2, 3) with radius 1 and a Script at (2, 2) with radius 0 should be close enough to touch (just barely).", one.matches(zero));
        
        Script xOnly = new Script(3, -1, null, null);
        
        assertTrue("If either Script has -1 for the y component, only the x difference should be used to find distance.", xOnly.matches(one));
        assertFalse("If either Script has -1 for the y component, only the x difference should be used to find distance.", xOnly.matches(zero));
        assertTrue("If either Script has -1 for the y component, only the x difference should be used to find distance.", xOnly.matches(two));
        assertTrue("If either Script has -1 for the y component, only the x difference should be used to find distance.", xOnly.matches(three));
        assertFalse("Be careful that you use the absolute value of the difference when calculating the distance for just one coordinate.", xOnly.matches(new Script(4, 2, null, null)));
        
        Script yOnly = new Script(-1, 3, null, null);
        
        assertTrue("If either Script has -1 for the x component, only the y difference should be used to find distance.", yOnly.matches(one));
        assertFalse("If either Script has -1 for the x component, only the y difference should be used to find distance.", yOnly.matches(zero));
        assertTrue("If either Script has -1 for the x component, only the y difference should be used to find distance.", yOnly.matches(two));
        assertTrue("If either Script has -1 for the x component, only the y difference should be used to find distance.", yOnly.matches(three));
    }
    
/* * * * * Beginning of part 2 * * * * */
    
    /**
     * Test that when a creature adds or removes itself to a level, it does in fact show up as a script
     * in the new level and leave its old level. Test also that when a script is added to or removed from
     * a level, its |level| is set to the new level, or |null| if it is in no level.
     * 
     * This test depends on being able to do the basic search for the first match to a script.
     */
    public void testAddAndRemove() {
        Level level1 = new Level(), level2 = new Level();
        Script a = new Script(1, 2, null, null);
        
        // Test the methods that work from the |Level| side of things.
        assertTrue("A newly created script should have level null; it is not in any level yet.", a.getLevel() == null);
        level1.addScript(a);
        assertTrue("Adding a script to a level with addScript() should change the level of that script.", a.getLevel() == level1);
        assertTrue("Adding a script to a level should make it visible to getScript() in that level.", level1.getScript(a) == a);
        level1.removeScript(a);
        assertTrue("Removing a script from a level should set its level to null.", a.getLevel() == null);
        assertTrue("If a script has been removed from a level, I should not find it when I ask that level to getScript().", level1.getScript(a) == null);
        level2.addScript(a);
        level1.addScript(a);
        assertTrue("If I add a script to another level, it should be removed from the level it is in.", level2.getScript(a) == null);
        assertTrue("If I add a script to another level, it should appear inthe new level.", level1.getScript(a) == a);
        
        a.move(2, 3, level2);
        assertTrue("The move(x, y, level) method should change the x, y, and level of the script.", a.getLevel() == level2 && a.getX() == 2 && a.getY() == 3);
        assertTrue("Moving a script into a level should make it visible to getScript() in that level.", level2.getScript(a) == a);
        level2.addScript(new Script(6, 7, null, null));
        assertTrue("Calling getScript(script) should return the script that was added first.", level2.getScript(a) == a);
        a.move(6, 7, level2);
        assertTrue("Moving a script within a level should not change its list position in that level.", level2.getScript(a) == a);
        a.move(2, 3, null);
        assertTrue("Moving a script to a null level should set its level to null.", a.getLevel() == null);
        assertTrue("Moving a script to a null level should remove it from its level.", level2.getScript(a) == null);
        
        Script b = new Script(2, 3, null, null);
        level1.addScript(b);
        a.move(b);
        assertTrue("If I move a script to a script in a different level, its level should change.", a.getLevel() == level1);
        assertTrue("If I move a script to a script in a different level, its x and y should now match that script.", a.getX() == 2 && a.getY() == 3);
        assertTrue("If I move a script to a script in a different level, it should be removed from the level it is in.", level2.getScript(a) == null);
        b.move(4, 5);
        assertTrue("If I move a script to a script in a different level, it should appear in the new level.", level1.getScript(a) == a);
        b.remove();
        a.move(b);
        assertTrue("Moving a script to a script in a null level should set its level to null.", a.getLevel() == null);
        assertTrue("Moving a script to a script in a null level should remove it from its level.", level1.getScript(a) == null);
    }
    
    /**
     * Test that scripts can be added to a level and that the three searching methods can be used to
     * locate scripts within that level.
     */
    public void testSearching() {
        Script a = new Script(1, 1, null, null);
        Script b = new Script(1, 2, null, null);
        Script c = new Script(1, 3, null, null);
        Script d = new Script(2, 2, null, null);
        Script e = new Script(1, 1, null, null);
        Script n = new Script(9, 9, null, null);
        
        Level level = new Level();
        level.addScript(a);
        level.addScript(b);
        level.addScript(c);
        level.addScript(d);
        level.addScript(e);
        
        // Test the basic getScript
        assertTrue("The script added first should be the one returned by the simple getScript(script).", level.getScript(a) != e);
        assertTrue("Using a script itself as a template should always locate that script in a level.", level.getScript(a) == a);
        assertTrue("If a script is not in a level, getScript(script) should return null.", level.getScript(n) == null);
        
        // Test the array getScripts()
        ArrayList scripts = level.getScripts(new Script(1, -1, null, null));
        assertTrue("The getScripts(script) method should return an array of all the scripts that match the given template.", scripts != null && scripts.size() == 4);
        assertTrue("The getScripts(script) method should scripts listed in the order they were added.", scripts.get(0) == a && scripts.get(1) == b && scripts.get(2) == c && scripts.get(3) == e);
        scripts = level.getScripts(n);
        assertTrue("If there are no scripts matching the template, |getScripts()| should return an empty ArrayList.", scripts != null && scripts.isEmpty());
        
        // Test getScript(n, script)
        assertTrue("The getScript(n, script) method should return matches in the order they were added to the level.", level.getScript(0, a) == a && level.getScript(1, a) == e);
        assertTrue("The getScript(n, script) method should return null if the index is greater than or equal to the number of matches.", level.getScript(2, a) == null);
        assertTrue("The getScript(n, script) method should return null if the index is negative.", level.getScript(-1, a) == null);
        assertTrue("The getScript(n, script) method should return null if the script is not present.", level.getScript(0, n) == null);
    }
    
    public void testItemContainer() {
        ItemContainer container = new ItemContainer();
        Item swords = new Item(new Script(0, 0, "Item", "Sword:c3"));
        Item shields = new Item(new Script(0, 0, "Item", "Shield:c1"));
        
        assertTrue("Oops - there is some mistake in Mr. Z.'s part of the code.", swords.getName().equals("Sword"));
        assertTrue("Oops - there is some mistake in Mr. Z.'s part of the code.", swords.getCount() == 3);
        
        container.addItem(swords);
        container.addItem(shields);
        
        ArrayList list = container.getItems();
        assertTrue("When items are added with addItem() they should show up in the array returned by getItems(), in the same order in which they were added.",
            list.size() == 2 && ((Item)list.get(0)).getName().equals("Sword") && ((Item)list.get(0)).getCount() == 3 && ((Item)list.get(1)).getName().equals("Shield") && ((Item)list.get(1)).getCount() == 1);
        
        Item item = container.getItem("Sword");
        assertTrue("I should be able to get an item in the container by passing its name to getItem().", item != null && item.getName().equals("Sword") && item.getCount() == 3);
        assertTrue("Passing the name of an item not in the container to getItem() should return null.", container.getItem("Helmet") == null);
        
        container.addItem(new Item(new Script(0, 0, "Item", "Sword:c3")));
        item = container.getItem("Sword");
        assertTrue("Adding an item that is already in the container should add to the count of the item already there.", item.getCount() == 6);
        assertTrue("Adding an item that is already in the container should not change the number of items in the container", container.getItems().size() == 2);
        
        // Test removing
        Item removed = container.removeItem("Sword", 2);
        item = container.getItem("Sword");
        assertTrue("Removing some, but not all, of an item should return an item with the count requested.", removed != null && removed.getName().equals("Sword") && removed.getCount() == 2);
        assertTrue("Removing some, but not all, of an item should leave that Item still in the container, but with a reduced count.", item != null && item.getCount() == 4);
        removed = container.removeItem("Sword", 4);
        assertTrue("Removing all of an item should return an item with the count requested.", removed != null && removed.getName().equals("Sword") && removed.getCount() == 4);
        assertTrue("Removing a number of an item equal to the number in the container should remove that item from the container's list.", container.getItem("Sword") == null);
        removed = container.removeItem("Shield", 2);
        item = container.getItem("Shield");
        assertTrue("Trying to remove more of an item than exist in the container should return null.", removed == null);
        assertTrue("Trying to remove more of an item than exist in the container should not change anything in the container; the attempt simply fails.", item != null && item.getCount() == 1);
        removed = container.removeItem("Shield", -1);
        item = container.getItem("Shield");
        assertTrue("Passing -1 as the parameter to removeItem() should automatically remove whatever number of that item are in the container.", removed != null && removed.getCount() == 1);
        assertTrue("Passing -1 as the parameter to removeItem() should never leave any of that item in the container.", item == null);
        list = container.getItems();
        assertTrue("I've removed everything from this container; why does getItems() not return an empty list?", list != null && list.isEmpty());
        
        container.addItem(new Item(new Script(0, 0, "Item", "Helmet:c-1")));
        assertTrue("Adding an item with count -1 to the container means that the container contains an unlimitted amount of that item. Your container does not appear to have accepted this item.", container.getItem("Helmet") != null);
        assertTrue("If I add an item with count -1 to the container, it should have this same count when in the container.", container.getItem("Helmet").getCount() == -1);
        container.addItem(new Item(new Script(0, 0, "Item", "Helmet:c18")));
        assertTrue("If an item in the container has count -1, this count should not change when a new item is added.", container.getItem("Helmet").getCount() == -1);
        removed = container.removeItem("Helmet", 25);
        assertTrue("If an item in the container has count -1, I should be able to remove any number of it.", removed != null);
        assertTrue("The amount of an object removed, even if there were -1 of it in the container, should be the number I asked for.", removed.getCount() == 25);
    }
    
/* * * * * Beginning of part 3 * * * * */

    private String hexByte(int b) {
        String hex = Integer.toHexString(b).toUpperCase();
        if(hex.length() == 1) {
            return "0" + hex;
        } else {
            return hex;
        }
    }

    private void checkTiles(String time, Level level, int[] expected) {
        for(int i = 0; i < 32; i++) {
            Tile tile = level.getTile(i % 8, i / 8);
            assertTrue(time + "The tile at (" + (i % 8) + ", " + (i / 8) + ") in your level should have been of number "
                + expected[i] + " (" + hexByte(expected[i]) + ") but your level says it is type "
                + tile.getNumber() + " (" + hexByte(tile.getNumber()) + ").", tile.getNumber() == expected[i]);
        }
    }
    
    /**
     * Test the Level methods that deal with tiles: loadTiles(), which reads in the hexadecimal tile
     * information from a file; getTile(), which retrieves the Tile at a given location, and setTile(),
     * which replaces the tile at a given location. This is the test for the first section of part 3.
     */
    public void testTiles() {
        Level level = Level.testLoadTiles();
        assertTrue("The test level does not appear to have loaded. Make sure that \"test.txt\" is in your RPG directory, and check the terminal for errors that might have occurred in your constructor.", level != null);
        
        int[] expected = new int[32];
        for(int i = 0; i < 32; i++) {
            expected[i] = i;
        }
        
        checkTiles("On initial loading: ", level, expected);
        
        assertTrue("Asking for a tile outside the level should return null.", level.getTile(-1, 3) == null);
        assertTrue("Asking for a tile outside the level should return null.", level.getTile(3, -1) == null);
        assertTrue("Asking for a tile outside the level should return null.", level.getTile(8, 3) == null);
        assertTrue("Asking for a tile outside the level should return null.", level.getTile(4, 4) == null);
        
        level.setTile(2, 3, Tile.getTile(100));
        expected[8*3 + 2] = 100;
        level.setTile(3, 2, Tile.getTile(150));
        expected[8*2 + 3] = 150;
        level.setTile(4, 1, Tile.getTile(200));
        expected[8*1 + 4] = 200;
        level.setTile(5, 0, Tile.getTile(250));
        expected[8*0 + 5] = 250;
        
        checkTiles("After using setTile() to change some tiles: ", level, expected);
        
        level.setTile(-1, 3, Tile.getTile(99));
        level.setTile(3, -1, Tile.getTile(99));
        level.setTile(8, 2, Tile.getTile(99));
        level.setTile(0, 4, Tile.getTile(99));
        
        checkTiles("After setting to 99 some tiles that were outside the level and hence should have changed nothing: ", level, expected);
    }
    
    /**
     * Test out the Script method createScript, which is supposed to parse a string and turn it
     * into a Script. What makes this complicated is that if the script type is Creature, Item,
     * or Scenery, this method is supposed to use the script it has created to construct an object
     * of one of those classes, and return that object instead.
     */
    public void testCreateScript() {
        Script script = Script.createScript("1 3 Lever 2 2 Test Town");
        String explanation = "I created a script \"1 3 Lever 2 2 Test Town\", but ";
        assertTrue(explanation + "createScript() returned null.", script != null);
        assertTrue(explanation + "the script is at (" + script.getX() + ", " + script.getY() + 
            ") instead of (1, 3).", script.getX() == 1 && script.getY() == 3);
        assertTrue(explanation + "the script has the wrong action: " + script.getAction(),
            script.getAction().equals("Lever"));
        assertTrue(explanation + "the script has the wrong argument: " + script.getArgument(),
            script.getArgument().equals("2 2 Test Town"));
        
        script = Script.createScript("1 1 Creature Player:s3d4c5");
        explanation = "I created a script \"1 1 Creature Player:s3d4c5\", but ";
        assertTrue(explanation + "createScript() returned null.", script != null);
        assertTrue(explanation + "the script is at (" + script.getX() + ", " + script.getY() + 
            ") instead of (1, 1).", script.getX() == 1 && script.getY() == 1);
        assertTrue(explanation + "the script has the wrong action: " + script.getAction(),
            script.getAction().equals("Creature"));
        assertTrue(explanation + "the script is not a Creature; it is a: " + script.getClass().getName()
            + ". Remember that if a script is of type Creature, Item, or Scenery, you are supposed to first make a Script for it, then pass that script along to the constructor of the appropriate class.",
            script instanceof Creature);
        assertTrue(explanation + "the Creature is not named \"Player\"",
            ((Creature)script).getName().equals("Player"));
        
        script = Script.createScript("4 2 Item Helmet:c2");
        explanation = "I created a script \"4 2 Item Helmet:c2\", but ";
        assertTrue(explanation + "createScript() returned null.", script != null);
        assertTrue(explanation + "the script is at (" + script.getX() + ", " + script.getY() + 
            ") instead of (4, 2).", script.getX() == 4 && script.getY() == 2);
        assertTrue(explanation + "the script has the wrong action: " + script.getAction(),
            script.getAction().equals("Item"));
        assertTrue(explanation + "the script is not an Item; it is a: " + script.getClass().getName()
            + ". Remember that if a script is of type Creature, Item, or Scenery, you are supposed to first make a Script for it, then pass that script along to the constructor of the appropriate class.",
            script instanceof Item);
        assertTrue(explanation + "the Creature is not named \"Helmet\"",
            ((Item)script).getName().equals("Helmet"));
        
        script = Script.createScript("6 1 Scenery Tree:");
        explanation = "I created a script \"6 1 Scenery Tree:\", but ";
        assertTrue(explanation + "createScript() returned null.", script != null);
        assertTrue(explanation + "the script is at (" + script.getX() + ", " + script.getY() + 
            ") instead of (6, 1).", script.getX() == 6 && script.getY() == 1);
        assertTrue(explanation + "the script has the wrong action: " + script.getAction(),
            script.getAction().equals("Scenery"));
        assertTrue(explanation + "the script is not a Scenery; it is a: " + script.getClass().getName()
            + ". Remember that if a script is of type Creature, Item, or Scenery, you are supposed to first make a Script for it, then pass that script along to the constructor of the appropriate class.",
            script instanceof Scenery);
        assertTrue(explanation + "the Scenery is not named \"Tree\"",
            ((Scenery)script).getName().equals("Tree"));
    }
    
    /**
     * This is a complete test of the level loading code that you have developed for part 3. It will use
     * the Level() constructor that takes a BufferedReader, and will ask it to load a test level, complete
     * with terrain and scripts. Then, it will check that all that information was loaded correctly.
     */
    public void testLoading() {
        Level level = Level.testLoadFullLevel();
        
        // Test that the basic level information was correctly loaded
        assertTrue("The test level should have loaded the name \"Test Town\" from the first line of the reader. Its name is \""
            + level.getName() + "\".", level.getName().equals("Test Town"));
        assertTrue("The test level should have loaded its width and height from the reader: a width of 8 and a height of 4. Instead is has a width of "
            + level.getWidth() + "and a height of " + level.getHeight() + ".", level.getWidth() == 8 && level.getHeight() == 4);
        
        int[] expected = new int[32];
        for(int i = 0; i < 32; i++) {
            expected[i] = i;
        }
        checkTiles("", level, expected);
            
        Script script = level.getScript(new Script(1, 3, null, null));
        String explanation = "The test level should have a script at (1, 3) with action \"Lever\" and argument \"2 2 Test Town\". ";
        assertTrue(explanation + "There is no script there.", script != null);
        assertTrue(explanation + "The script there has the wrong action: " + script.getAction(),
            script.getAction().equals("Lever"));
        assertTrue(explanation + "The script there has the wrong argument: " + script.getArgument(),
                script.getArgument().equals("2 2 Test Town"));
        assertTrue(explanation + "The script there has the wrong level: " + script.getLevel(),
                script.getLevel() == level);
        
        script = level.getScript(new Script(1, 1, null, null));
        explanation = "The test level should have a Creature at (1, 3) with name \"Player\". ";
        assertTrue(explanation + "There is no script there.", script != null);
        assertTrue(explanation + "The script there has the wrong action: " + script.getAction(),
            script.getAction().equals("Creature"));
        assertTrue(explanation + "The script there is not a Creature; it is a: " + script.getClass().getName()
            + ". Remember that if a script is of type Creature, Item, or Scenery, you are supposed to first make a Script for it, then pass that script along to the constructor of the appropriate class.",
            script instanceof Creature);
        assertTrue(explanation + "The Creature there is not named \"Player\"",
            ((Creature)script).getName().equals("Player"));
        assertTrue(explanation + "The script there has the wrong level: " + script.getLevel(),
                script.getLevel() == level);
        
        script = level.getScript(new Script(4, 2, null, null));
        explanation = "The test level should have an Item at (4, 2) with name \"Helmet\". ";
        assertTrue(explanation + "There is no script there.", script != null);
        assertTrue(explanation + "The script there has the wrong action: " + script.getAction(),
            script.getAction().equals("Item"));
        assertTrue(explanation + "The script there is not an Item; it is a: " + script.getClass().getName()
            + ". Remember that if a script is of type Creature, Item, or Scenery, you are supposed to first make a Script for it, then pass that script along to the constructor of the appropriate class.",
            script instanceof Item);
        assertTrue(explanation + "The Creature there is not named \"Helmet\"",
            ((Item)script).getName().equals("Helmet"));
        assertTrue(explanation + "The script there has the wrong level: " + script.getLevel(),
                script.getLevel() == level);
        
        script = level.getScript(new Script(6, 1, null, null));
        explanation = "The test level should have a Scenery at (6, 1) with name \"Tree\". ";
        assertTrue(explanation + "There is no script there.", script != null);
        assertTrue(explanation + "The script there has the wrong action: " + script.getAction(),
            script.getAction().equals("Scenery"));
        assertTrue(explanation + "The script there is not a Scenery; it is a: " + script.getClass().getName()
            + ". Remember that if a script is of type Creature, Item, or Scenery, you are supposed to first make a Script for it, then pass that script along to the constructor of the appropriate class.",
            script instanceof Scenery);
        assertTrue(explanation + "The Scenery there is not named \"Tree\"",
            ((Scenery)script).getName().equals("Tree"));
        assertTrue(explanation + "The script there has the wrong level: " + script.getLevel(),
                script.getLevel() == level);
    }
    
    public void testSaving() {
        Level level = Level.testLoadFullLevel();
        try {
            FileWriter writer = new FileWriter("./save.txt");
            level.writeToFile(writer);
            writer.close();
            
            BufferedReader testFile = new BufferedReader(new FileReader("./test.txt"));
            BufferedReader saveFile = new BufferedReader(new FileReader("./save.txt"));
            
            String test = testFile.readLine(), save = saveFile.readLine();
            int i = 1;
            while(test != null && save != null) {
                if(!test.equals(save)) {
                    assertTrue("Line number " + i + " does not match:\nTest: " + test + "\nSave: " + save, false);
                }
                i++;
                test = testFile.readLine();
                save = saveFile.readLine();
            }
            assertTrue("The saved file was of a different length than the test file. Did you remember that levels are supposed to end with a blank line?", test == save);
            
            testFile.close();
            saveFile.close();
        } catch(Exception e) {
            e.printStackTrace();
            assertTrue("An exception of type " + e.getClass().getName() + " was produced while saving.", false);
        }
    }
}