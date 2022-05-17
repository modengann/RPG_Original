package rpg;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Write a description of class RPGView here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class RPGView extends JPanel implements MouseListener {
    private int ox, oy;
    private int mx, my;
    
    public RPGView() {
        super();
        setPreferredSize(new Dimension(550, 660));
        addMouseListener(this);
    }
     
    public void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 550, 660);
        ((Graphics2D)g).setFont(new Font("Serif", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if(Map.getMap() == null) return;
        Creature player = Map.getMap().getPlayer();
        
        if(!Tile.hasLoaded() || player == null) return;
        
        int minx, miny, maxx, maxy;
        
        Level level = player.getLevel();
        if(level == null) return;
        
        if(level.getWidth() <= 11) {
            minx = 0;
            maxx = level.getWidth() - 1;
            ox = (11 - level.getWidth()) * Tile.tileWidth / 2;
        } else {
            if(player.getX() < 5) {
                minx = 0;
            } else if(player.getX() > level.getWidth() - 6) {
                minx = level.getWidth() - 11;
            } else {
                minx = player.getX() - 5;
            }
            maxx = minx + 10;
            ox = 0;
        }
        
        if(level.getHeight() <= 11) {
            miny = 0;
            maxy = level.getHeight() - 1;
            oy = (11 - level.getHeight()) * Tile.tileHeight / 2;
        } else {
            if(player.getY() < 5) {
                miny = 0;
            } else if(player.getY() > level.getHeight() - 6) {
                miny = level.getHeight() - 11;
            } else {
                miny = player.getY() - 5;
            }
            maxy = miny + 10;
            oy = 0;
        }
        ox -= Tile.tileWidth * minx;
        oy -= Tile.tileHeight * miny;
        g.translate(ox, oy);
        for(int j = miny; j <= maxy; j++) {
            for(int i = minx; i <= maxx; i++) {
                Tile t = level.getTile(i, j);
                if(t != null) t.draw(g, i, j);
            }
        }
        for(int j = miny; j <= maxy + 2; j++) {
            ArrayList scripts = level.getScripts(new Script(-1, j, "Field", null));
            for(int i = 0; i < scripts.size(); i++) {
                Script script = (Script)scripts.get(i);
                if(script.getX() >= minx && script.getY() <= maxy) Tile.getTile(Integer.parseInt(script.getArgument())).draw(g, script.getX(), script.getY());
            }
            
            scripts = level.getScripts(new Script(-1, j, null, null));
            for(int i = 0; i < scripts.size(); i++) {
                Script script = (Script)scripts.get(i);
                boolean onscreen = script.getX() >= minx - 1 && script.getX() <= maxx + 1 && 
                	script.getX() > 0 && script.getY() > 0;
                if(script instanceof VisibleObject) {
                	if(onscreen) ((VisibleObject)script).getTile().draw(g, script.getX(), script.getY());
                } else if(script.getX() < minx || script.getY() > maxy) {
                } else if(script.getAction().equals("Field")) {
                } else if(script.getAction().equals("Effect")) {
                    script.remove();
                    if(onscreen) {
	                    String arg = script.getArgument();
	                    int index = arg.indexOf(":");
	                    if(index == -1) {
	                        Tile t = Tile.getEffect(arg);
	                        if(t != null) t.draw(g, script.getX(), script.getY());
	                    } else {
	                        Tile t = Tile.getEffect(arg.substring(0, index));
	                        if(t != null) t.draw(g, script.getX(), script.getY());
	                        g.setColor(Color.BLACK);
	                        arg = arg.substring(index + 1);
	                        if(arg.length() > 0) g.drawString(arg, (int)((script.getX() + .5) * Tile.tileWidth - .5 * fm.stringWidth(arg) + .5), 
	                                                                (int)((script.getY() + .5) * Tile.tileHeight));
	                    }
                    }
                } else if(SpeechBubble.supportsType(script.getAction())) {
                    new SpeechBubble(script, this);
                    requestFocus();
                    repaint();
                    script.remove();
                } else if(script.getAction().equals("ConversationPanel")) {
                    new ConversationPanel(script, this);
                    requestFocus();
                    script.remove();
                } else if(script.getAction().equals("ContainerPanel")) {
                    new ContainerPanel(script, this);
                    requestFocus();
                    script.remove();
                } else if(script.getAction().equals("Menu")) {
                    RPGMenu menu = new RPGMenu();
                    menu.setPage(script.getArgument());
                    menu.runInView(this);
                    requestFocus();
                    repaint();
                    script.remove();
                } else if(j <= maxy) {
                    g.setColor(Color.WHITE);
                    g.drawRect(script.getX() * Tile.tileWidth + 2, script.getY() * Tile.tileHeight + 2, 6, 6);
                }
            }
        }
        g.translate(-ox, -oy);
    }
    
    /**
     * Return a Rectangle with the boundaries of the tile at the given location.
     * @param x x coordinate of tile
     * @param y y coordinate of tile
     * @return boundaries of tile
     */
    public Rectangle getTileRectangle(int x, int y) {
        return new Rectangle(x * Tile.tileWidth + ox, y * Tile.tileHeight + oy, Tile.tileWidth, Tile.tileHeight);
    }
    
    /* * * Implementation of MouseListener * * */
    public void mousePressed(MouseEvent event) {
        requestFocus();
    }
    public void mouseClicked(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
}
