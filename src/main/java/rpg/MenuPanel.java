package rpg;

import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.BasicStroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Arc2D;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedString;
import java.util.HashMap;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Write a description of class MenuPanel here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class MenuPanel extends JPanel implements XMLParser.ParseListener, MouseListener, MouseMotionListener {
    private static final double kRoundEdgesRadius = 6;
    private static final int kInset = 10;
    private static final int kMaxLinkLines = 32;
    private static final Color bgColor = new Color(0f, 0f, .6f);
    private static final Color linkColor = new Color(0f, 0f, .4f);
    private static final Color linkText = new Color(.6f, .6f, 1f);
    
    private static double[] right = new double[kMaxLinkLines];
    private static double[] left = new double[kMaxLinkLines];
    private static double[] top = new double[kMaxLinkLines + 1];
    
    private JScrollPane scrollView;
    private LinkRange links, selectedOption, hover = null;
    private int indicatorStart, indicatorEnd;
    private Object dataSource;
    
    private boolean parsingOption;
    private String parseString, parseLink, parseSign;
    
    private String text;
    private LineBreakMeasurer measurer;
    private boolean valid, changedText;
    private float width;
    
    private int[] breaks = new int[100];
    private int line;
    
    private void moreBreaks() {
        int[] b = new int[breaks.length + 50];
        for(int i = 0; i < breaks.length; i++) {
            b[i] = breaks[i];
        }
        breaks = b;
    }
    
    private void addBreak() {
        if(line >= breaks.length) moreBreaks();
        breaks[line++] = measurer.getPosition();
    }
    
    private class LinkRange {
        private int start, end;
        private Object link;
        private GeneralPath frame;
        private LinkRange next, prev;
        private String shortcut;
        
        /**
         * Create a new link frame, linked to itself only. Use add() to add
         * it into an existing list.
         */
        public LinkRange(Object link, String linkText) {
            this.start = text.length();
            text += linkText;
            this.end = text.length();
            this.link = link;
			shortcut = "";
            
            next = this;
            prev = this;
            frame = null;
        }
        
        /**
         * Insert the given link frame before this one
         */
        public void add(LinkRange range) {
            prev.next = range;
            range.prev = prev;
            range.next = this;
            prev = range;
        }
        
        /**
         * Get the next link frame
         */
        public LinkRange getNext() {
            return next;
        }
        
        /**
         * Get the previous link frame
         */
        public LinkRange getPrevious() {
            return prev;
        }
        
        /**
         * Return the frame of this link
         */
        public GeneralPath getFrame() {
            return frame;
        }
        
        /**
         * Set a keyboard shortcut for this link
         * @param s keyboard shortcut (name of key, capitalized if shift down, caret if control)
         */
        public void setShortcut(String s) {
        	shortcut = s;
        }
        
        /**
         * Recalculate the positions of all the link frames within the string given by the
         * "text" instance variable.
         */
        public void recalculate(AttributedString string, Graphics2D g) {
            setAttributes(string);
            
            measurer = new LineBreakMeasurer(string.getIterator(), g.getFontRenderContext());
            
            line = 0;
            width = scrollView.getViewport().getViewRect().width;
            buildFrames((int)kInset, (int)kInset);
        }
        
        /**
         * Notification that a change has been made in the text string at the given location, resulting in
         * all the frames after that needing to have their indices changed.
         */
        public void insert(int index, int changeInLength) {
            if(start > index) {
                start += changeInLength;
            }
            if(end > index) {
                end += changeInLength;
            }
            if(next.start > start) {
                next.insert(index, changeInLength);
            }
        }
        
        /**
         * Take apart this doubly linked list so that it can be garbage collected.
         */
        public void die() {
            if(prev != null) {
                prev = null;
                next.die();
                next = null;
            }
        }
        
        /**
         * Return the object identifying this link
         */
        public Object getLink() {
            return link;
        }
        
		/**
		 * Return the LinkRange that is identified by the given object, or null if
		 * there is no such LinkRange.
		 */
		public LinkRange select(Object id) {
			if(link.equals(id)) return this;
			else if(next.start > start) return next.select(id);
			else return null;
		}
        
		/**
		 * Return the LinkRange that is identified by the given object, or null if
		 * there is no such LinkRange.
		 * @param key keyboard shortcut that was entered
		 */
		public LinkRange key(String key) {
			if(shortcut.equals(key)) return this;
			else if(next.start > start) return next.key(key);
			else return null;
		}
        
        /**
         * Return the LinkRange at the given location, or null if there is nothing there
         */
        public LinkRange click(int x, int y) {
            if(frame != null && frame.contains(x, y)) return this;
            else if(next.start > start) return next.click(x, y);
            else return null;
        }
        
        /**
         * Change the text that appears for this link. This will possibly shift over all later links and thus will
         * require recalculating everything after this link.
         */
        public void changeText(String newText) {
            int offset = newText.length() - (end - start);
            if(indicatorStart != -1 && indicatorStart >= end) {
                indicatorStart += offset;
                indicatorEnd += offset;
            }
            next.offset(offset);
            text = text.substring(0, start) + newText + text.substring(end);
            end = start + newText.length();
        }
        
        /**
         * Draw the frame of this link.
         */
        public void drawFrame(Graphics2D g) {
            g.setColor(linkColor);
            g.fill(frame);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2f));
            g.draw(frame);
        }
        
        /**
         * Draw the frame of this link.
         */
        public void drawArea(Graphics2D g) {
            g.setColor(linkColor);
            g.fill(frame);
        }
        
        private void offset(int offset) {
            start += offset;
            end += offset;
            if(next.start > start) {
                next.offset(offset);
            }
        }

        private void setAttributes(AttributedString string) {
            string.addAttribute(TextAttribute.FOREGROUND, linkText, start, end);
            if(next.start > this.start) {
                next.setAttributes(string);
            }
        }
                
        private Arc2D arc(double centerX, double centerY, double radius, double startAngle, double angleExtent) {
            return new Arc2D.Double(centerX - radius, centerY - radius, 2 * radius, 2 * radius, startAngle, angleExtent, Arc2D.OPEN);
        }
        
        private void buildFrames(float x, float y) {
            float maxHeight = 0;
            // If we encounter a word that is too long for th eline, we don't want to get into an infinite loop.
            // So, don't word wrap if this line is empty.
            boolean lineContainsText = false, parsingLink = false;
            int endIndex = start, count = 0;
            
            // Layout up to the start of this option
            while(measurer.getPosition() < end) {
                int lineBreak = text.substring(measurer.getPosition()).indexOf("\n");
                TextLayout layout;
                
                if(lineBreak != -1) {
                    lineBreak = lineBreak + measurer.getPosition() + 1;
                    layout = measurer.nextLayout((float)(width - x - kInset), Math.min(endIndex, lineBreak), lineContainsText);
                } else {
                    layout = measurer.nextLayout((float)(width - x - kInset), endIndex, lineContainsText);
                }
                
                // If there is text to lay out, lay it out
                if(layout != null) {
                    maxHeight = Math.max(maxHeight, layout.getAscent() + layout.getDescent() + layout.getLeading());
                    x += layout.getAdvance();
                    lineContainsText = true;
                    if(parsingLink) {
                        right[count] = x;
                    }
                }
                
                // If there was no space left in this line, or we hit a "\n", go to the next line
                if(layout == null || measurer.getPosition() == lineBreak) {
                    // Next line
                    y += maxHeight;
                    maxHeight = 0;
                    x = (float)kInset;
                    lineContainsText = false;
                    
                    addBreak();
                    
                    if(parsingLink) {
                        count++;
                        top[count] = y;
                        right[count] = x;
                        left[count] = x;
                    }
                }
                
                // If we are ready to start creating the frame for this link
                if(!parsingLink && measurer.getPosition() == start) {
                    parsingLink = true;
                    endIndex = end;
                    top[0] = y;
                    right[0] = x;
                    left[0] = x;
                 }
            }
            if(lineContainsText) {
                count++;
                top[count] = y + (float)(maxHeight + .5);
            }
            
            frame = new GeneralPath();
            for(int i = 0; i < count; i++) { // Draw right edges
                if(i == 0 || right[i] >= right[i - 1] + 2 * kRoundEdgesRadius) { // Top hang out
                    frame.append(arc(right[i], top[i] + kRoundEdgesRadius, kRoundEdgesRadius, 90, -90), i != 0);
                } else if(right[i] <= right[i - 1] - 2 * kRoundEdgesRadius) { // Top hang in
                    frame.append(arc(right[i] + kRoundEdgesRadius * 2, top[i] + kRoundEdgesRadius, kRoundEdgesRadius, 90, 90), true);
                }
                
                if(i == count - 1 || right[i] >= right[i + 1] + 2 * kRoundEdgesRadius) { // Bottom hang out
                    frame.append(arc(right[i], top[i + 1] - kRoundEdgesRadius, kRoundEdgesRadius, 0, -90), true);
                } else if(right[i] <= right[i + 1] - 2 * kRoundEdgesRadius) { // Bottom hang in
                    frame.append(arc(right[i] + kRoundEdgesRadius * 2, top[i + 1] - kRoundEdgesRadius, kRoundEdgesRadius, 180, 90), true);
                }
            }     
            
            for(int i = count - 1; i >= 0; i--) { // Draw left edges
                if(i == count - 1 || left[i] <= left[i + 1] - 2 * kRoundEdgesRadius) { // Bottom hang out
                    frame.append(arc(left[i], top[i + 1] - kRoundEdgesRadius, kRoundEdgesRadius, 270, -90), true);
                } else if(left[i] >= left[i + 1] + 2 * kRoundEdgesRadius) { // Bottom hang in
                    frame.append(arc(left[i] - kRoundEdgesRadius * 2, top[i + 1] - kRoundEdgesRadius, kRoundEdgesRadius, 270, 90), true);
                }
                
                if(i == 0 || left[i] <= left[i - 1] - 2 * kRoundEdgesRadius) { // Top hang out
                    frame.append(arc(left[i], top[i] + kRoundEdgesRadius, kRoundEdgesRadius, 180, -90), true);
                } else if(left[i] <= left[i - 1] - 2 * kRoundEdgesRadius) { // Top hang in
                    frame.append(arc(left[i] - kRoundEdgesRadius * 2, top[i] + kRoundEdgesRadius, kRoundEdgesRadius, 0, 90), true);
                }
            }
            frame.closePath();
            // If not at the end of the option list, layout the next frame
            if(next.start > this.start) {
                next.buildFrames(x, y);
            } else {
                if(lineContainsText && measurer.getPosition() >= text.length()) {
                    addBreak();
                    y = (int)top[count];
                }
                while(measurer.getPosition() < text.length()) {
                    int lineBreak = text.substring(measurer.getPosition()).indexOf("\n");
                    TextLayout layout;
                    
                    if(lineBreak != -1) {
                        lineBreak += measurer.getPosition() + 1;
                        layout = measurer.nextLayout(getWidth() - x - kInset, lineBreak, false);
                    } else {
                        layout = measurer.nextLayout(getWidth() - x - kInset);
                    }
                    
                    x = kInset;
                    y += (int)(layout.getAscent() + layout.getDescent() + layout.getLeading() + .5);
                    addBreak();
                }
                y += kInset + .5;
                if(y < scrollView.getViewport().getViewRect().height) y = scrollView.getViewport().getViewRect().height;
                
                breaks[line] = 0; // signal for last break
                
                setPreferredSize(new Dimension(getWidth(), (int)(y)));
                revalidate();
            }
        }
    }
    
    public Rectangle getView() {
        return scrollView.getViewport().getViewRect();
    }
    
    private void doLink(Object link) {
        try {
            dataSource.getClass().getMethod("doLink", new Class[] {MenuPanel.class, Object.class}).invoke(dataSource, new Object[] {this, link});
            repaint();
        } catch(Exception e) {
        }
    }
    
    public void doMenuPanelLayout() {
        try {
            dataSource.getClass().getMethod("doLayout", new Class[] {MenuPanel.class}).invoke(dataSource, new Object[] {this});
            repaint();
        } catch(Exception e) {
        }
    }
    
    private void changedSelection() {
        try {
            dataSource.getClass().getMethod("changedSelection", new Class[] {MenuPanel.class, Object.class}).invoke(dataSource, new Object[] {this, selectedOption.getLink()});
        } catch(Exception e) {
        }
        repaint();
    }
    
    public void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D)graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
        g.setColor(bgColor);
        g.fill(scrollView.getViewport().getViewRect());
        
        if(text.equals("")) return;
        if(!valid || changedText) {
            AttributedString string = new AttributedString(text);
            string.addAttribute(TextAttribute.FAMILY, "Serif");
            string.addAttribute(TextAttribute.SIZE, new Float(20f));
            string.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
            string.addAttribute(TextAttribute.FOREGROUND, Color.WHITE);

            changedText = false;
            
            if(!valid) {
                if(links != null) links.recalculate(string, g);
                else measurer = new LineBreakMeasurer(string.getIterator(), g.getFontRenderContext());
                
                valid = true;
            } else {
                if(links != null) links.setAttributes(string);
                measurer = new LineBreakMeasurer(string.getIterator(), g.getFontRenderContext());
            }
        }
        
        if(hover != null && hover != selectedOption) hover.drawArea(g);
        if(selectedOption != null) selectedOption.drawFrame(g);
       
        float y = (float)kInset;
        
        measurer.setPosition(0);
        line = 0;
        while (measurer.getPosition() < text.length()) {
            TextLayout layout = measurer.nextLayout(800, breaks[line++], false);
            
            layout.draw(g, (float)kInset, y + layout.getAscent());
            y += layout.getAscent() + layout.getDescent() + layout.getLeading();
        }
        g.setStroke(new BasicStroke(3f));
        g.setColor(Color.WHITE);
        g.draw(scrollView.getViewport().getViewRect());
    }
    
    /**
     * This method is called when the space bar has ben pressed. If the selected link is on
     * screen, this activates it; otherwise, it scrolls down.
     */
    public void keySpaceReleased() {
		if(linkByKey("space")) return;
		
        JViewport viewPort = scrollView.getViewport();
        if(selectedOption == null || !viewPort.getViewRect().intersects(selectedOption.getFrame().getBounds())) {
            Point p = viewPort.getViewPosition();
            p.y += viewPort.getViewSize().height - 20;
            if(p.y >= getHeight() - viewPort.getViewSize().height) p.y = getHeight() - viewPort.getViewSize().height;
            viewPort.setViewPosition(p);
        } else {
            doLink(selectedOption.getLink());
        }
    }
    
    private void scrollToVisible() {
        JViewport viewPort = scrollView.getViewport();
        
        if(selectedOption == null) return;
        
        Rectangle clipBounds = viewPort.getViewRect();
        Rectangle optionBounds = selectedOption.getFrame().getBounds();
        
        boolean down = optionBounds.y > clipBounds.y;
        
        int y;
        if(down) {
            y = optionBounds.y + optionBounds.height - clipBounds.height;
            if(clipBounds.y >= y) return;
        } else {
            y = optionBounds.y;
            if(clipBounds.y <= y) return;
        }
        viewPort.setViewPosition(new Point(0, y));
        repaint();
        scrollView.repaint();
    }
    
    public void keyDownReleased() {
		if(linkByKey("down")) return;
		
        if(selectedOption != null) selectedOption = selectedOption.getNext();
        changedSelection();
        scrollToVisible();
    }
    
    public void keyUpReleased() {
		if(linkByKey("up")) return;
		
        if(selectedOption != null) selectedOption = selectedOption.getPrevious();
        changedSelection();
        scrollToVisible();
    }
    
    public void keyMReleased() {
		if(linkByKey("m")) return;
		
        try {
            dataSource.getClass().getMethod("doMenuKey", new Class[] {MenuPanel.class}).invoke(dataSource, new Object[] {this});
        } catch(Exception e) {
        }
    }
    
    private boolean linkByKey(String key) {
		LinkRange link = links.key(key);
		if(link != null) {
			selectedOption = link;
			changedSelection();
			doLink(selectedOption.getLink());
			return true;
		} else {
			return false;
		}
    }
    
	public void otherKeyReleased(String key) {
		linkByKey(key);
	}
    
	public void keyLeftReleased() {
		if(linkByKey("left")) return;
		
		try {
			dataSource.getClass().getMethod("doLeftKey", new Class[] {MenuPanel.class}).invoke(dataSource, new Object[] {this});
		} catch(Exception e) {
		}
	}
    
    public void keyRightReleased() {
		if(linkByKey("right")) return;
		
        try {
            dataSource.getClass().getMethod("doRightKey", new Class[] {MenuPanel.class}).invoke(dataSource, new Object[] {this});
        } catch(Exception e) {
        }
    }
    
    /**
     * Construct a new MenuPanel in the given view, with the given bounds. It will automatically create a scroll
     * pane to contain itself.
     * @param view View to put the scroll pane in
     * @param bounds bounds of the scroll pane
     */
    public MenuPanel(JPanel view, Rectangle bounds) {
        super();
        
        setLayout(null);
        
        clear();
        
        scrollView = new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        view.add(scrollView);
        scrollView.setBounds(bounds);
        scrollView.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        setSize(new Dimension(bounds.width, bounds.height));
        revalidate();
        
        addMouseListener(this);
        addMouseMotionListener(this);
        
        Map.getMap().getKey().pushTarget(this);
    }
    
    /**
     * Clear this MenuPanel. This is typically a preparation to lay out a new page.
     */
    public void clear() {
        if(links != null) {
            links.die();
            links = null;
            selectedOption = null;
            hover = null;
        }
        
        text = "";
        
        indicatorStart = -1;
        
        valid = false;
    }
    
    /**
     * Add the given string to the end of this MenuPanel, as ordinary text.
     * @param string text to add
     */
    public void addString(String string) {
        text += string;
        valid = false;
    }
    
    /**
     * Add a link to the end of this MenuPanel. A default sign of "\n>" will be appended before the link; if you want to change
     * this behavior use the other version of this method that allows you to set a sign.
     * @param link object identifying this link
     * @param linkText text to appear in the panel for this link
     */
    public void addLink(Object link, String linkText) {
        addLink(link, linkText, "\n> ");
    }
    
	/**
	 * Add a link to the end of this MenuPanel, with the given sign in plain text before it.
	 * @param link object identifying this link
	 * @param linkText text to appear in the panel for this link
	 * @param sign sign to append before this link
	 */
	public void addLink(Object link, String linkText, String sign) {
		addString(sign);
		LinkRange frame = new LinkRange(link, linkText);
		if(links == null) {
			links = frame;
			selectedOption = frame;
		} else {
			links.add(frame);
		}
		valid = false;
	}
    
	/**
	 * Add a link to the end of this MenuPanel, with the given sign in plain text before it.
	 * @param link object identifying this link
	 * @param linkText text to appear in the panel for this link
	 * @param sign sign to append before this link
	 */
	public void addLink(Object link, String linkText, String sign, String key) {
		addString(sign);
		LinkRange frame = new LinkRange(link, linkText);
		frame.setShortcut(key);
		if(links == null) {
			links = frame;
			selectedOption = frame;
		} else {
			links.add(frame);
		}
		valid = false;
	}
    
    /**
     * Append the given string, and mark it as the receiver's indicator. The indicator is a section of text that can
     * be replaced easily with setIndicator(), without redoing the layout. There can be only one indicator, so if you set a new
     * one, the old one becomes static text. The space taken up by the indicator should not change, since the layout is not
     * recalculated for a change in the indicator.
     * @param string String to append as indicator.
     */
    public void addIndicator(String string) {
        indicatorStart = text.length();
        text += string;
        indicatorEnd = text.length();
        valid = false;
    }
    
    /**
     * Replace the receiver's indicator with the given string, without redoing the layout. The space taken up
     * by the new sting should be the same as the old.
     * @param string String to replace indicator with.
     */
    public void setIndicator(String string) {
        if(indicatorStart == -1) return;
        text = text.substring(0, indicatorStart) + string + text.substring(indicatorEnd);
        
        int offset = string.length() - (indicatorEnd - indicatorStart);
        if(links != null) links.insert(indicatorStart, offset);
        
        int line = string.indexOf("\n");
        int lineStart = indicatorStart;
        
        for(int i = 0; i < breaks.length; i++) {
            if(breaks[i] == 0 && i != 0) break;
            if(breaks[i] > indicatorStart && breaks[i] <= indicatorEnd) {
                if(line != -1) {
                    lineStart += line + 1;
                    breaks[i] = lineStart;
                    string = string.substring(line + 1);
                    line = string.indexOf("\n");
                } else {
                    breaks[i] = indicatorEnd;
                }
            } else if(breaks[i] > indicatorEnd) {
                breaks[i] += offset;
            }
            if(breaks[i] > text.length()) {
                breaks[i] = text.length();
                break;
            }
        }
        
        indicatorEnd += offset;
        
        changedText = true;
        repaint();
    }
    
    /**
     * The string given to this method should contain an XML-formatted specification of this page. The only two
     * tags you need to use are <option link="(name)" sign="(sign)">(link text)</option> and <br/> for line breaks.
     * The link object sent to the delegate will be a String with the text you entered as (name).
     * @param string String to parse as XML.
     */
    public void layoutXML(String string) {
        XMLParser parser = new XMLParser(string, this);
        clear();
        parser.parse();
        valid = false;
    }
    
    public void startElement(String name, HashMap attributes) {
        if(name.equals("br")) {
            if(parsingOption) parseString += "\n";
            else text += "\n";
        } else if(name.equals("option")) {
            parsingOption = true;
            parseString = "";
            
            parseLink = (String)attributes.get("link");
            parseSign = (String)attributes.get("sign");
            
            if(parseLink == null) parseLink = "Root";
            if(parseSign == null) parseSign = "\n> ";
        }
    }
    
    public void endElement(String name) {
        if(name.equals("option") && parsingOption) {
            if(!parseString.equals("")) {
                addLink(parseLink, parseString, parseSign);
            } else {
                addLink(parseLink, parseLink, parseSign);
            }
            parsingOption = false;
        }
    }
    
    public void readText(String text) {
        if(parsingOption) {
            parseString += text;
        } else {
            this.text += text;
        }
    }
    
    /**
     * Write a method with this signature for the dataSource class if you want to be able
     * to control how a page is laid out. This method should add content to the page using
     * a combination of addString(), addLink(), and addIndicator.
     * @param panel the panel to be laid out
     */
    public void doLayout(MenuPanel panel) {
    }
    
    /**
     * Write a method with this signature for the dataSource class if you want to be able
     * to control what happens when a link is chosen. A typical response would be to lay out
     * a new page. This method is called if the player clicks on a link or presses the space
     * bar while a link is seelcted and visible.
     * @param panel the panel in which the event occurred
     * @param link the link that was chosen
     */
    public void doLink(MenuPanel panel, Object link) {
    }
    
    /**
     * The player has selected the given link, either by clicking on it or by selecting it
     * with the up and down arrows. This method is sent before doLink() if it was clicking
     * that caused it
     * @param panel the panel in which the event occurred
     * @param link the link that is now selected
     */
    public void changedSelection(MenuPanel panel, Object link) {
    }
    
    /**
     * Return the object identifying the selected link.
     * @return the object identifying the selected link
     */
    public Object getSelectedLink() {
        if(selectedOption == null) return null;
        return selectedOption.getLink();
    }
    
    /**
     * Select the link identified by the given object. This
     * will result in selectionChanged() being called for the data source.
     * @param link Object identifying the link to select
     */
    public boolean selectLink(Object link) {
        LinkRange range = links.select(link);
        
        if(range == null) return false;
        
        changedSelection();
        scrollToVisible();
        repaint();
        return true;
    }
    
    /**
     * Return the data source
     * @return the data source
     */
    public Object getDataSource() {
        return dataSource;
    }
    
    /**
     * Change the data source
     * @param source new data source
     */
    public void setDataSource(Object source) {
        dataSource = source;
    }
    
    /**
     * Change the text of the given link. This will not affect its sign. This will require the menu panel to recalculate
     * the bounding boxes of all its links.
     * @param link object identifying the link
     * @param linkText new link text
     */
    public void setLinkText(Object link, String linkText) {
        if(links == null) return;
        LinkRange range = links.select(link);
        if(range == null) return;
        range.changeText(linkText);
        
        valid = false;
        repaint();
    }
    
    /**
     * Remove this menu from the view.
     */
    public void goodbye() {
        Map.getMap().getKey().popTarget(this);
        Container c = scrollView.getParent();
        c.remove(scrollView);
        c.repaint();
    }
    
    /* * * Implementation of MouseListener * * */
    public void mousePressed(MouseEvent event) {
        if(hover != null) {
            if(hover != selectedOption) {
                selectedOption = hover;
                changedSelection();
            }
            doLink(selectedOption.getLink());
        }
    }
    public void mouseClicked(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {
        if(hover != null) {
            hover = null;
            repaint();
        }
    }
    
    /* * * Implementation of MouseMotionListener * * */
    public void mouseMoved(MouseEvent event) {
        if(links == null) return;
        LinkRange range = links.click(event.getX(), event.getY());
        if(range == hover) return;
        hover = range;
        repaint();
    }
    public void mouseDragged(MouseEvent event) {}
}