package rpg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import javax.swing.JPanel;
import java.util.ArrayList;

/**
 * A Targetter is a JPanel layer that temporarily appears over the main view
 * to do targetting. It shows what space you are targeting, and allows selection
 * of targets using the mouse or the keyboard. All creatures within the view
 * are given letters for keyboard targetting.
 */
public class Targeter extends JPanel implements MouseListener, MouseMotionListener {
	private boolean mouseInView;
	private int mx, my;
	private ArrayList targets;
	private Creature player;
	private RPGView view;
	private String method;
	private int minx, miny;
	private Creature hover;
	
	private static Color[][] colors = new Color[][] {{Color.BLUE, Color.CYAN}, 
													 {new Color(0x006600), Color.GREEN},
													 {new Color(0x660000), Color.RED}};
	private void addTarget(Creature c) {
		int r = (c.getX() - player.getX()) * (c.getX() - player.getX()) +
				(c.getY() - player.getY()) * (c.getY() - player.getY());
		for(int i = 0; i < targets.size(); i++) {
			Creature o = (Creature)targets.get(i);
			int d = (o.getX() - player.getX()) * (o.getX() - player.getX()) +
					(o.getY() - player.getY()) * (o.getY() - player.getY());
			if(r < d) {
				targets.add(i, c);
				return;
			}
		}
		targets.add(c);
	}
	
	public Targeter(RPGView view, String method) {
		player = Map.getMap().getPlayer();
		this.view = view;
		this.method = method; 
		
		targets = new ArrayList();
		
		Level level = player.getLevel();
		Script t = new Script(0, 0, "Creature", null);
		Creature c;
		
		int width = view.getWidth() / Tile.tileWidth;
		int height = view.getHeight() / Tile.tileHeight;
		int minx = player.getX() - (width - 1) / 2;
		int miny = player.getY() - (height - 1) / 2;
		for(int i = minx; i < minx + width; i++) {
			for(int j = miny; j < miny + height; j++) {
				t.move(i, j);
				if(level.getTile(i, j) != null &&
						(c = (Creature)level.getScript(t)) != null) {
					addTarget(c);
				}
			}
		}
		
		setOpaque(false);
		view.add(this);
		setBounds(0, 0, view.getWidth(), view.getHeight());
		validate();
		view.repaint();
		
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		
		Map.getMap().getKey().pushTarget(this);
	}
	
	private void goodbye() {
		view.remove(this);
		Map.getMap().getKey().popTarget(this);
		view.repaint();
	}
	
	public void otherKeyReleased(String key) {
		if(key.equals("escape")) {
			goodbye();
			return;
		} else if(key.length() != 1) {
			return;
		}
		char c = key.charAt(0);
		int i = -1;
		
		if(c >= 'a' && c <= 'z') {
			i = c - 'a';
		} else if(c >= 'A' && c <= 'Z') {
			i = c - 'A' + 26;
		} else if(c >= '0' && c <= '9') {
			i = c - '0' + 52;
		}
		
		if(i == -1 || i >= targets.size()) return;
		
		try {
			player.getClass().getMethod(method, new Class[] {Creature.class}).invoke(player, new Object[] {targets.get(i)});
		} catch(Exception e) {
			e.printStackTrace();
		}
		goodbye();
	}
	
	private Shape textShape(Graphics2D g, Font font, String text) {
		String line;
		GeneralPath outline = null;
		while(text.length() > 0) {
			int i = text.indexOf("\n");
			if(i == -1) {
				line = text;
				text = "";
			} else {
				line = text.substring(0, i);
				text = text.substring(i + 1);
			}
			if(line.length() == 0) break;
			Shape s = font.createGlyphVector(g.getFontRenderContext(), line).getOutline();
			s = AffineTransform.getTranslateInstance(-s.getBounds().x - s.getBounds().width / 2,
													 -s.getBounds().y).createTransformedShape(s);
			if(outline == null) {
				outline = new GeneralPath(s);
			} else {
				s = AffineTransform.getTranslateInstance(0, outline.getBounds().height + 2).createTransformedShape(s);
				outline.append(s, false);
			}
		}
		return outline;
	}
	
	public void paintComponent(Graphics g) {
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Font font = new Font("Serif", Font.BOLD, 20);
		char a = 'a';
		
		for(int i = 0; i < targets.size(); i++) {
			Creature c = (Creature)targets.get(i);
			Rectangle r = view.getTileRectangle(c.getX(), c.getY());
			
			Shape outline;
			if(c == hover) {
				outline = textShape((Graphics2D)g, font, c.getShortDescription());
			} else {
				outline = textShape((Graphics2D)g, font, "(" + a + ")");
			}
			
			AffineTransform t = AffineTransform.getTranslateInstance(
				r.x + .5 * r.width, r.y + .5 * r.height - .5 * outline.getBounds().height);
			outline = t.createTransformedShape(outline);
			Color[] color;
			if(c.isPlayer()) {
				color = colors[0];
			} else if(c.isFriendly()) {
				color = colors[1];
			} else {
				color = colors[2];
			}
			g.setColor(color[0]);
			((Graphics2D)g).draw(outline);
			g.setColor(color[1]);
			((Graphics2D)g).fill(outline);
			
			if(a == 'z') {
				a = 'A';
			} else if(a == 'Z') {
				a = '0';
			} else if(a == '9') {
				return;
			} else {
				a++;
			}
		}
	}
	
	public void mouseDragged(MouseEvent event) { }
	public void mouseMoved(MouseEvent event) {
		int dx = event.getX() / Tile.tileWidth;
		int dy = event.getY() / Tile.tileHeight;
		if(dx == mx && dy == my) return;
		
		mx = dx;
		my = dy;
		
		hover = null;
		for(int i = 0; i < targets.size(); i++) {
			Creature c = (Creature)targets.get(i);
			Rectangle rect = view.getTileRectangle(c.getX(), c.getY());
			if(rect.contains(event.getX(), event.getY())) {
				hover = c;
				break;
			}
		}
		
		repaint();
	}
	
	public void mouseClicked(MouseEvent event) {
		mouseMoved(event);
		if(hover == null) return;
		try {
			player.getClass().getMethod(method, new Class[] {Creature.class}).invoke(player, new Object[] {hover});
		} catch(Exception e) {
			e.printStackTrace();
		}
		goodbye();
	}
	
	public void mousePressed(MouseEvent event) { }
	public void mouseReleased(MouseEvent event) { }
	public void mouseEntered(MouseEvent event) { mouseInView = true; }
	public void mouseExited(MouseEvent event) { mouseInView = false; mx = -1; my = -1; hover = null; repaint(); }
}
