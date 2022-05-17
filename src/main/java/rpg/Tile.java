package rpg;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.Color;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.util.List;

/**
 * A Tile represents anything that has an image and can appear in a level. The first 256 tiles are reserved for terrain tiles; the tiles after
 * that represent creatures, scenery, items, effects, and so on.
 * 
 * Each tile knows how to draw itself in the view at a given (x, y) location. This location would be the same as the (x, y) location of the
 * script or terrain square that this tile represents. Each tile also has a collection of flags (boolean values) and parameters (int values),
 * each with a letter associated with it, that it can report back when asked.
 * 
 * There are a few methods that you will definitely need to know how to use:
 * 
 * <b>Tiles and tile number:</b>
 * <code>Tile Tile.getTile(int number)</code> - Retrieve the tile of a given number: needed when loading maps
 * <code>int getNumber()</code> - Ask a tile what number it is (the reverse of the above)
 * 
 * <b>Getting information about a tile:</b>
 * <code>boolean isSolid()</code> - Does this tile block creatures from moving through it
 * <code>boolean isOpaque()</code> - Does this tile block creatures from seeing through it
 * <code>boolean canAction()</code> - Does this tile turn into something else when actioned
 * <code>boolean isSolid()</code> - Does this tile turn into something else when levered
 * <code>Tile become()</code> - What does this tile turn into when actioned or levered?
 * 
 * @author Russell Zahniser
 * @version 2006-02-05
 */
public class Tile {
    private static ArrayList tiles = null;
    private static ArrayList imageFiles;
    private static HashMap matchingClasses;
    private static ArrayList palettes;
    public static final int tileWidth = 50;
    public static final int tileHeight = 60;
    private static boolean hasLoaded = false;
    private static Palette effectsPalette = null;
    
    private int tile;          // What tile number is this?
    private String name;       // What is its name?
    private String specs;      // What other parameters does it have?
    private ImageFile image;   // What image file does it use?
    private int frame;         // What frame in that file is it?
    private String matching;   // For terrain tiles only - how it matches terrain near it
    private Tile next;         // next random equivalent of this tile
    private int index;          // index within random class
    
    private static class Palette {
        private int start, end;
        private String name;
        
        public Palette(int start, String name) {
            this.start = start;
            this.end = -1;
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public void setEnd(int end) {
            this.end = end;
        }
        
        public int getStart() {
            return start;
        }
        
        public int getEnd() {
            return end;
        }
        
        public List getTiles() {
            return tiles.subList(start, end);
        }
    }

    public static class ImageFile implements ImageObserver {
    	private boolean loaded = false, failed = false;
        private ArrayList images;
        private int xAdjust, yAdjust, length, width, height, flags;
        private String fileName;
        private Image image;

        private static final int NEEDS_FLAGS = ImageObserver.ALLBITS | ImageObserver.HEIGHT | ImageObserver.WIDTH;
        
    	private static HashMap imageFiles = new HashMap();

    	private ImageFile(String parameters) {
    		super();
    		parseParameters(parameters);
    		imageFiles.put(fileName, this);
    		image = null;
    	}
    	
    	private void parseParameters(String parameters) {
            int i = parameters.indexOf(" ");
            if(i == -1) {
                fileName = "rpgImages/" + parameters;
                width = tileWidth;
                height = tileHeight;
                length = -1;
            } else {
                fileName = "rpgImages/" + parameters.substring(0, i);
                parameters = parameters.substring(i + 1);
                
                i = parameters.indexOf(" ");
                if(i == -1) {
                    length = Integer.parseInt(parameters);
                    width = tileWidth;
                    height = tileHeight;
                } else {
                    length = Integer.parseInt(parameters.substring(0, i));
                    parameters = parameters.substring(i + 1);
                    
                    i = parameters.indexOf(" ");
                    width = Integer.parseInt(parameters.substring(0, i));
                    height = Integer.parseInt(parameters.substring(i + 1));
                }
            }
            
            xAdjust = (tileWidth - width) / 2;
            if(height < tileHeight) {
                yAdjust = (tileHeight - height) / 2;
            } else {
                yAdjust = tileHeight - height;
            }
    	}

    	private void loadImage() {
    		failed = true;
    		BufferedInputStream is = new BufferedInputStream(Tile.class.getResourceAsStream(fileName));
    		try {
    			byte[] buf = new byte[is.available()];
    			is.read(buf);

    			image = Toolkit.getDefaultToolkit().createImage(buf);
    			flags = 0;
    			Toolkit.getDefaultToolkit().prepareImage(image, -1, -1, this);
    			
    			failed = false;
    		} catch(IOException e) {
    			System.err.println("Error in reading from " + fileName);
    		} finally {
    			try {
    				is.close();
    			} catch(IOException e) {
    				System.err.println("Unable to close " + fileName);
    			}
    		}
    	}
    	
    	private void imageLoaded() {
    		if(loaded || (flags & NEEDS_FLAGS) != NEEDS_FLAGS) return;
            
            BufferedImage bufferedCopy = new BufferedImage(image.getWidth(this), image.getHeight(this), 
            												BufferedImage.TYPE_INT_ARGB);
            Graphics bufgc = bufferedCopy.createGraphics();
            bufgc.drawImage(image, 0, 0, this);
            
            images = new ArrayList();
            int columns = image.getWidth(this) / width;
            
            for(int i = 0; i < length; i++) {
                images.add(bufferedCopy.getSubimage((i % columns) * width, 
                		(i / columns) * height, width, height));
            }
            loaded = true;
    	}
    	
    	private boolean load() {
    		if(image == null && !failed) {
    			loadImage();
    		}
    		return loaded;
    	}

    	public void draw(int image, Graphics g, int x, int y) {
    		if(load()) {
                g.drawImage((Image)images.get(image), x * tileWidth + xAdjust, y * tileHeight + yAdjust, null);
    		} else {
    			g.setColor(Color.WHITE);
    			g.drawString("Loading...", x * tileWidth, y * tileHeight + tileHeight / 2);
    		}
    	}

    	public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
    		this.flags |= flags;
    		imageLoaded();
    		return !loaded;
    	}
        
        /**
         * Return the image at the given position in this file.
         * @param image Image number to return
         */
        public Image getImage(int image) {
            if(load()) {
            	return (Image)images.get(image);
            } else {
            	return null;
            }
        }
    }
    
    /**
     * Create a new tile representing the given argument, with the given tile number. Only the tile loader should pass a number
     * for the tile; all tiles that are simply created within a map should have number -1.
     */
    public Tile(String specs, int tile) {
        super();
        
        this.tile = tile;
        
        int i = specs.indexOf(":");
        if(i != -1) {
            name = specs.substring(0, i);
            this.specs = specs.substring(i + 1);
        } else {
            name = specs;
            this.specs = "";
        }
        // Am I derived from some other tile?
        i = getParameter("t");
        if(i != -1) {
            if(name.equals("")) name = getTile(i).name;
            this.specs += getTile(i).specs;
        }
        
        // Do I have matching information? (Only valid for terrain tiles)
        i = name.indexOf(".");
        if(i != -1) {
            matching = name.substring(i + 1);
            name = name.substring(0, i);
        } else {
            matching = null;
        }
        // These are set by the loading code if it determines that this tile is in a random class
        next = this;
        index = -1;
        
        // Locate my image file and frame
        i = getParameter("i");
        if(i == -1 || i >= imageFiles.size()) {
            image = null;
        } else {
            image = (ImageFile)imageFiles.get(i);
            frame = getParameter("f");
        }
    }
    
    public Tile getParent() {
        int i = getParameter("t");
        if(i != -1) return getTile(i);
        else return null;
    }
    
    /**
     * Can this object be seen? That is, does it have an image associated with it?
     * @return true if the object is visible, false otherwise.
     */
    public boolean isVisible() {
        return image != null;
    }
    
    public Image getImage() {
        if(image != null) return image.getImage(frame);
        else return null;
    }
    
    /**
     * Draw this tile at the given x and y coordinate in the given graphics. It will automatically be centered in the x coordinate.
     * If it is shorter than a tile, it will also be centered in the y coordinate; if it is taller than a tile, it will be drawn
     * with its base along the base of its tile.
     * 
     */
    public void draw(Graphics g, int x, int y) {
        if(image != null) image.draw(frame, g, x, y);
    }
    
    /**
     * Get the name of this tile.
     * @return the name of this tile
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the number of this tile.
     * @return the number of this tile
     */
    public int getNumber() {
        return tile;
    }
    
    /**
     * Determine whether this tile is solid (blocks movement)
     * @return whether this tile is solid
     */
    public boolean isSolid() {
        return (specs.indexOf("S") != -1);
    }
    
    /**
     * Determine whether this tile is opaque (blocks sight)
     * @return whether this tile is opaque
     */
    public boolean isOpaque() {
        return (specs.indexOf("O") != -1);
    }
    
    /**
     * Determine whether this tile changes when actioned
     * @return whether this tile changes when actioned
     */
    public boolean canAction() {
        return (specs.indexOf("A") != -1);
    }
    
    /**
     * Determine whether this tile changes when it is the target of a lever
     * @return whether this tile changes when it is the target of a lever
     */
    public boolean canLever() {
        return (specs.indexOf("L") != -1);
    }
    
    /**
     * Determine what tile this tile becomes when it is changed by an action or lever.
     */
    public Tile getBecome() {
        int become = getParameter("b");
        if(become == -1) return this;
        Tile t = getTile(become);
        if(t.getType().equals("Field")) t = getEffect(t.getName());
        return t;
    }
    
    /**
     * Determine whether this tile has a specified flags. Flags should be single capital letters.
     * @param flag flag to look for
     * @return value of this flag
     */
    public boolean getFlag(String flag) {
        return (specs.indexOf(flag) != -1);
    }
    
    /**
     * Set the value of the given flag.
     * @param flag Flag whose value should be set
     * @param set New value to give it
     */
    public void setFlag(String flag, boolean set) {
        if(set && specs.indexOf(flag) == -1) specs += flag;
        else if(!set && specs.indexOf(flag) != -1) {
            int i = specs.indexOf(flag);
            if(i < specs.length() - 1) {
                specs = specs.substring(0, i) + specs.substring(i + 1);
            } else {
                specs = specs.substring(0, i);
            }
        }
    }
    
    public String toString() {
        return name + ":" + specs;
    }
    
    /**
     * Assign a new value to one of this Tile's parameters.
     * @param parameter parameter to change
     * @param value new value to give it
     */
    public void setParameter(String parameter, int value) {
        int start = specs.indexOf(parameter), end = start + 1;
        char c;
        while(end < specs.length() && ((c = specs.charAt(end)) == '-' || (c >= '0' && c <= '9'))) end++;
        specs = specs.substring(0, start + 1) + value + specs.substring(end);
    }
    
    /**
     * Determine the value of the given parameter for this tile. Parameter names should be single
     * lowercase letters.
     * @param parameter parameter to retrieve value for
     * @return value of this parameter, or -1 if it is not present
     */
    public int getParameter(String parameter) {
        int value = 0;
        boolean negative = false, positive = false, valid = false, field = false;
        int i = specs.indexOf(parameter);
        if(i == -1 || i >= specs.length() - 1) return -1;
        
        i++;
        if(specs.substring(i, i + 1).equals("+")) {
            positive = true;
            i++;
        } else if(specs.substring(i, i + 1).equals("*")) {
            field = true;
            i++;
        }
        if(specs.substring(i, i + 1).equals("-")) {
            negative = true;
            i++;
        }
        char c;
        while(i < specs.length() && (c = specs.charAt(i++)) >= '0' && c <= '9') {
            value *= 10;
            value += c - '0';
            valid = true;
        }
        if(!valid) return -1;
        
        if(negative) value = -value;
        
        if(positive && tile >= 0) value = tile + value;
        else if(field && tile >= 0 && effectsPalette != null) value = effectsPalette.getStart() + value;
        
        return value;
    }
    
    /**
     * What type of object (Tile, Creature, etc) does this tile represent?
     */
    public String getType() {
        for(int i = 0; i < palettes.size(); i++) {
            if(tile < ((Palette)palettes.get(i)).getEnd()) {
                String name = ((Palette)palettes.get(i)).getName();
                int index = name.indexOf(" ");
                if(index == -1) {
                    return name;
                } else {
                    return name.substring(0, index);
                }
            }
        }
        return "Tile";
    }
    
    private static boolean matchesVertical(String match) {
        int i = ((String)matchingClasses.get(match)).indexOf("v");
        return i >= 0 && i < 2;
    }
    
    private static boolean matchesHorizontal(String match) {
        int i = ((String)matchingClasses.get(match)).indexOf("h");
        return i >= 0 && i < 2;
    }
    
    public static Tile getEffect(String name) {
        if(effectsPalette == null) return null;
        
        ArrayList possibilities = new ArrayList();
        
        for(int i = effectsPalette.getStart(); i < effectsPalette.getEnd(); i++) {
            Tile t = (Tile)tiles.get(i);
            if(t.getName().equalsIgnoreCase(name)) possibilities.add(t);
        }
        if(possibilities.isEmpty()) return null;
        else return (Tile)possibilities.get((int)(Math.random() * possibilities.size()));
    }
    
    private static int match(String a, String b) {
        int m = 0;
        for(int i = 0; i < 4; i++) {
            if(a.charAt(i) == b.charAt(i)) m++;
        }
        return m;
    }
    
    private static int[] corner = {0, 1, 4, 3};
    
    public static void match(Level level, int x, int y) {
        String[] border = new String[9];
        // Get the matching information for all the tiles one space to either side of this one
        for(int j = -1; j <= 1; j++) {
            for(int i = -1; i <= 1; i++) {
                Tile tile = level.getTile(x + i, y + j);
                if(tile == null) {
                    border[i + 3*j + 4] = null;
                } else {
                    border[i + 3*j + 4] = tile.matching;
                }
            }
        }
        for(int c = 0; c < 4; c++) {
            String n = border[4].substring(c, c+1);
            if(matchesHorizontal(n)) {
                if(matchesVertical(n)) {
                    for(int b = 0; b < 4; b++) {
                        String m = border[corner[c] + corner[b]];
                        if(m != null) {
                            border[corner[c] + corner[b]] = m.substring(0, (b + 2) % 4) + n + m.substring((b + 2) % 4 + 1);
                        }
                    }
                } else {
                    if(c == 0 || c == 3) {
                        String m = border[3];
                        if(m != null) border[3] = m.substring(0, c ^ 1) + n + m.substring((c ^ 1) + 1);
                    } else {
                        String m = border[5];
                        if(m != null) border[5] = m.substring(0, c ^ 1) + n + m.substring((c ^ 1) + 1);
                    }
                }
            } else if(matchesVertical(n)) {
                if(c == 0 || c == 1) {
                    String m = border[1];
                    if(m != null) border[1] = m.substring(0, c ^ 3) + n + m.substring((c ^ 3) + 1);
                } else {
                    String m = border[7];
                    if(m != null) border[7] = m.substring(0, c ^ 3) + n + m.substring((c ^ 3) + 1);
                }
            }
        }
        border[4] = null;
        ArrayList replacements = new ArrayList();
        for(int j = -1; j <= 1; j++) {
            for(int i = -1; i <= 1; i++) {
                String m = border[j*3 + i + 4];
                if(m == null) continue;
                
                Tile oldTile = level.getTile(x + i, y + j);
                if(oldTile == null) continue;
                
                int best = match(m, oldTile.matching);
                if(best == 4) continue;
                
                replacements.clear();
                
                for(int t = 0; t < 256; t++) {
                    Tile tile = (Tile)tiles.get(t);
                    int a = match(m, tile.matching);
                    if(a > best) {
                        best = a;
                        replacements.clear();
                        replacements.add(tile);
                    } else if(a == best) {
                        replacements.add(tile);
                    }
                }
                if(!replacements.contains(oldTile)) {
                    int t = (int)(Math.random() * replacements.size());
                    level.setTile(x + i, y + j, (Tile)replacements.get(t));
                }
            }
        }
    }
    
    /**
     * Return another random tile that is equivalent to this one.
     * @return a randomized version of this tile
     */
    public Tile getRandom() {
        if(next == this) {
            return next;
        } else {
            Tile tile = this;
            while(tile.next.index > tile.index) tile = tile.next;
            int i = (int)(Math.random() * (tile.index - tile.next.index + 1)) + tile.next.index;
            
            tile = tile.next;
            while(true) {
                if(i < tile.next.index || tile.next.index < tile.index) {
                    return tile;
                }
                tile = tile.next;
            }
        }
    }
    
    private static String readLine(BufferedReader reader) {
        String line = "";
        while(line != null && (line.equals("") || line.equals(" "))) {
            try {
                line = reader.readLine();
            } catch(Exception e) {
                return null;
            }
            if(line != null && line.indexOf("//") != -1) line = line.substring(0, line.indexOf("//"));
        }
        return line;
    }
    
    /**
     * This method is called by the applet to load your tiles.
     * @param applet the RPGApplet
     */
    public static void loadTiles() {
        if(tiles != null) return;
        
        BufferedReader reader = null;
        HashMap randomClasses = new HashMap();
        try {
        	reader = new BufferedReader(new InputStreamReader(Tile.class.getResourceAsStream("rpgImages/tiles.txt")));
            String line = "0";
            int i = 0;
            
            tiles = new ArrayList();
            imageFiles = new ArrayList();
            matchingClasses = new HashMap();
            palettes = new ArrayList();
            
            boolean loaded = false;
            while(!loaded) {
                line = readLine(reader);
                i = line.indexOf(" ");
                if(line.indexOf("image files") != -1) {
                    loaded = true;
                } else {
                    matchingClasses.put(line.substring(0, i), line.substring(i + 1));
                }
            }
            
            int files = Integer.parseInt(line.substring(0, i));
            while(files > 0) {
                imageFiles.add(new ImageFile(readLine(reader)));
                files--;
            }
            
            Palette palette = null;
            while((line = readLine(reader)) != null) {
                Tile tile = new Tile(line, tiles.size());
                if(tile.name.equals("Begin Type")) {
                    if(palette != null) palette.setEnd(tiles.size());
                                            
                    palette = new Palette(tiles.size(), tile.specs);
                    if(tile.specs.equals("Field")) {
                        effectsPalette = palette;
                    }
                    palettes.add(palette);
                    
                    continue;
                }
                
                tiles.add(tile);
                if(tile.matching != null && (i = tile.matching.indexOf(".")) != -1) {
                    tile.index = Integer.parseInt(tile.matching.substring(i + 1));
                    tile.matching = tile.matching.substring(0, i);
                    
                    Tile lastInRandomClass = (Tile)randomClasses.get(tile.matching);
                    if(lastInRandomClass != null) {
                        tile.next = lastInRandomClass.next;
                        lastInRandomClass.next = tile;
                    }
                    
                    randomClasses.put(tile.matching, tile);
                }
            }
            if(palette != null) palette.setEnd(tiles.size());
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
        	if(reader != null) {
        		try {
					reader.close();
				} catch (IOException e) {
				}
        	}
        }
        hasLoaded = true;
    }
    
    public static boolean hasLoaded() {
        return hasLoaded;
    }
    
    /**
     * This is the method that you will use when you have an int value and want to get that tile.
     * It returns a Tile object.
     * @param tile number of tile to return
     * @return the Tile object with that number
     */
    public static Tile getTile(int tile) {
        return (Tile)tiles.get(tile);
    }
}