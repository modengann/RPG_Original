package rpg;

import javax.swing.JApplet;
import javax.swing.BoxLayout;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Class RPGApplet - write a description of the class here
 * 
 * @author Russell Zahniser
 * @version 2005-10-01
 */
public class RPGApplet extends JApplet {
    private RPGView view;
    
    public void init() {
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
        
        view = new RPGView();
        getContentPane().add(view);
        setSize(550, 660);
        
        Tile.loadTiles();
        
        try {
            loadMap(RPGApplet.class.getResourceAsStream("rpgMaps/map.txt"));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Load the map at the given URL
     * @param levelFile level file locating the map
     */
    public void loadMap(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        new Map(reader, view);
        reader.close();
        view.requestFocus();
        repaint();
    }
    
    public void stop() {
    }

    public void start() {
        view.requestFocus();
        repaint();
    }
}