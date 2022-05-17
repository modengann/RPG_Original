package rpg;

import java.awt.Color;
import java.awt.Container;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.BasicStroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedString;

/**
 * Write a description of class SpeechBubble here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class SpeechBubble extends JPanel {
    private static final double kBoundaryWidth = 20;
    private static final double kBoundaryHeight = 8;
    private static final double kTailHeight = 30;
    private static final double kArcOffset = (kTailHeight * .8660254 * 2 / 3);
    private static final double kHeightPadding = (2 * (kTailHeight + kBoundaryHeight));
    private static final double kWidthPadding = 2 * kBoundaryWidth;
    private static final double kMinimumWidth = 100;
    private static final double kIncrement = 10;
    private static final double kPreferredCloudWidth = 30;
    private static final double kCloudEndRadius = 10;
    private static final double kCloudEndLength = (kCloudEndRadius / 1.4142135);
    private static final double kRadiansToDegrees = (180. / 3.1415926535);
    private static final Color bgColor = new Color(0f, 0f, .6f);
    
    private float width;
    private Script speaker;
    private GeneralPath framePath;
    private LineBreakMeasurer measurer;
    
    private double heightForWidth(double width) {
        double height = 0;
        measurer.setPosition(0);
        while (measurer.getPosition() < speaker.getArgument().length()) {
            TextLayout layout = measurer.nextLayout((float)width);

            height += layout.getAscent() + layout.getDescent() + layout.getLeading();
        }
        return height;
    }

    private boolean isBubble() {
        return speaker.getAction().indexOf("Bubble") != -1;
    }

    private void placePanel() {
        Dimension viewRect = getParent().getSize();
        double availableWidth = viewRect.width - 2 * kBoundaryWidth;
        double availableHeight = heightForWidth(availableWidth);
        double idealRatio = viewRect.height / (double)viewRect.width / 3.;
        double width = availableWidth;
        double error = 10000;
    
        if(availableHeight > viewRect.height / 3.) {
            idealRatio = viewRect.height / (double)viewRect.width;
        }
        
        while(width >= kMinimumWidth) {
            double tmp = Math.abs(idealRatio - heightForWidth(width) / width);
            if(tmp < error) {
                error = tmp;
                availableWidth = width;
            }
            width -= kIncrement;
        }
        availableHeight = heightForWidth(availableWidth) + kHeightPadding;
        this.width = (float)availableWidth;
        availableWidth += 2 * kBoundaryWidth;
        
        setBounds((int)(viewRect.width - availableWidth) / 2, 0, (int)availableWidth, (int)availableHeight);
    }
    
    private void placeBubble() {
        Dimension viewRect = getParent().getSize();
        Rectangle sourceRect = ((RPGView)getParent()).getTileRectangle(speaker.getX(), speaker.getY());
        boolean isOnBottom = true;
        
        // First, try layout as covering the whole width of the screen. This gives us a lower limit on how much
        // height we need, which we can use to decide on top or bottom display.
        double availableWidth;
        double availableHeight;
        double offsetFromCenter;
        
        double tmp = heightForWidth(viewRect.width - 2 * kBoundaryWidth);
        
        availableHeight = sourceRect.y + sourceRect.height - kHeightPadding;
        // If there isn't enough space available on top, draw bubble below speaker.
        if(availableHeight < tmp) {
            isOnBottom = false;
            availableHeight = viewRect.height - (sourceRect.y + sourceRect.height) - kHeightPadding;
        }
        
        // offset of speaker center from screen center
        offsetFromCenter = sourceRect.x + .5 * sourceRect.width - .5 * viewRect.width;
        // Largest possible width centered on speaker
        availableWidth = viewRect.width - 2 * Math.abs(offsetFromCenter) - kWidthPadding;
        
        if(availableWidth < kMinimumWidth || heightForWidth(availableWidth) > availableHeight) { // Centering is impossible
            if(availableWidth < kMinimumWidth) availableWidth = kMinimumWidth;
            
            while(heightForWidth(availableWidth) > availableHeight) {
                availableWidth += kIncrement;
            }
            
            availableHeight = heightForWidth(availableWidth);
            // Draw at edge of view
            setLocation((int)(offsetFromCenter > 0 ? viewRect.width - availableWidth - 2 * kBoundaryWidth : 0),
                                    (int)(sourceRect.y - (isOnBottom ? availableHeight + kHeightPadding : 0)));
        } else {
            double idealRatio = availableHeight / availableWidth;
            double width = availableWidth;
            double error = 10000;
            
            while(width >= kMinimumWidth) {
                tmp = Math.abs(idealRatio - heightForWidth(width) / width);
                if(tmp < error) {
                    error = tmp;
                    availableWidth = width;
                }
                width -= kIncrement;
            }
            availableHeight = heightForWidth(availableWidth);
            
            setLocation((int)(sourceRect.x + .5 * (sourceRect.width - availableWidth) - kBoundaryWidth),
                                    (int)(sourceRect.y - (isOnBottom ? availableHeight + kHeightPadding : 0)));
        }
        this.width = (float)availableWidth;
        setSize((int)(availableWidth + kWidthPadding), (int)(availableHeight + kHeightPadding + 1));
    }
    
    private void buildSignFrame() {
        Rectangle frameRect = getBounds();
        Rectangle sourceRect = ((RPGView)getParent()).getTileRectangle(speaker.getX(), speaker.getY());
        boolean isOnBottom = (frameRect.y > sourceRect.y);
        
        framePath = new GeneralPath();
        
        // This whole business is greatly complicated by the fact that the text view inexplicably draws in a right-side-up
        // coordinate system. Yet the angle is still indexed as if it were all upside down. Or is that right-side up? Yes, the
        // angle is indexed as is fit and proper in math, but not in computer screens.
        
        framePath.moveTo(2f, (float)(2 + kTailHeight));
        
        if(isBubble() && isOnBottom) {
            framePath.lineTo((float)(sourceRect.x - frameRect.x + .5 * sourceRect.width - .25 * kTailHeight), (float)(kTailHeight + 2));
            framePath.lineTo((float)(sourceRect.x - frameRect.x + .5 * sourceRect.width), 2f);
            framePath.lineTo((float)(sourceRect.x - frameRect.x + .5 * sourceRect.width + .25 * kTailHeight), (float)(kTailHeight + 2));
        }
        
        framePath.lineTo((float)(frameRect.width - 2), (float)(2 + kTailHeight));
        framePath.lineTo((float)(frameRect.width - 2), (float)(frameRect.height - 2 - kTailHeight));
        
        if(isBubble() && !isOnBottom) {
            framePath.lineTo((float)(sourceRect.x - frameRect.x + .5 * sourceRect.width + .25 * kTailHeight), (float)(frameRect.height - kTailHeight - 2));
            framePath.lineTo((float)(sourceRect.x - frameRect.x + .5 * sourceRect.width), (float)(frameRect.height - 2));
            framePath.lineTo((float)(sourceRect.x - frameRect.x + .5 * sourceRect.width - .25 * kTailHeight), (float)(frameRect.height - kTailHeight - 2));
        }
        
        framePath.lineTo(2f, (float)(frameRect.height - 2 - kTailHeight));
        framePath.closePath();
    }
    
    private Arc2D arc(double centerX, double centerY, double radius, double startAngle, double angleExtent) {
        return new Arc2D.Double(centerX - radius, centerY - radius, 2 * radius, 2 * radius, startAngle, angleExtent, Arc2D.OPEN);
    }
    
    private void buildThoughtFrame() {
        Rectangle frameRect = getBounds();
        Rectangle sourceRect = ((RPGView)getParent()).getTileRectangle(speaker.getX(), speaker.getY());
        double width = frameRect.width - kWidthPadding;
        double height = frameRect.height - kHeightPadding;
        double wx = (width - 2 * kCloudEndLength) / (int)(((width - 2 * kCloudEndLength) / kPreferredCloudWidth) + 1.);
        double wy = (height - 2 * kCloudEndLength) / (int)(((height - 2 * kCloudEndLength) / kPreferredCloudWidth) + 1.);
        double xang = kRadiansToDegrees * Math.atan(2 * kCloudEndLength / wx);
        double yang = kRadiansToDegrees * Math.atan(2 * kCloudEndLength / wy);
        double rx = .5 * Math.sqrt(wx * wx + 2 * kCloudEndRadius * kCloudEndRadius);
        double ry = .5 * Math.sqrt(wy * wy + 2 * kCloudEndRadius * kCloudEndRadius);
        
        framePath = new GeneralPath();
        
        double x = kBoundaryWidth, y = kBoundaryHeight + kTailHeight;
        
        framePath.append(arc(x, y, kCloudEndRadius, 225, -180), true);
        
        for(x += kCloudEndLength + wx / 2; x < width + kBoundaryWidth - kCloudEndLength - wx / 2 + 1; x += wx) {
            framePath.append(arc(x, y, rx, (180. - xang), 2 * xang - 180), true);
        }
        
        x += kCloudEndLength - wx / 2;
        framePath.append(arc(x, y, kCloudEndRadius, 135, -180), true);
        
        for(y += kCloudEndLength + wy / 2; y < height + kBoundaryHeight + kTailHeight - kCloudEndLength - wy / 2 + 1; y += wy) {
            framePath.append(arc(x, y, ry, (90. - yang), 2 * yang - 180), true);
        }
        
        y += kCloudEndLength - wy / 2;
        framePath.append(arc(x, y, kCloudEndRadius, 45, -180), true);
        
        for(x -= kCloudEndLength + wx / 2; x > kBoundaryWidth + kCloudEndLength + wx / 2 - 1; x -= wx) {
            framePath.append(arc(x, y, rx, -xang, 2 * xang - 180), true);
        }
        
        x -= kCloudEndLength - wx / 2;
        framePath.append(arc(x, y, kCloudEndRadius, 315, -180), true);
        
        for(y -= kCloudEndLength + wy / 2; y > kBoundaryHeight + kTailHeight + kCloudEndLength + wy / 2 - 1; y -= wy) {
            framePath.append(arc(x, y, ry, (270. - yang), 2 * yang - 180), true);
        }
        
        framePath.closePath();
        
        if(!isBubble()) {
        } else if(frameRect.y < sourceRect.y) { // Bubble above speaker
            if(sourceRect.x > frameRect.x + frameRect.width / 2) { // tail at left
                framePath.append(new Ellipse2D.Double(sourceRect.x - frameRect.x - 4, frameRect.height - 6, 5, 5), false);
                framePath.append(new Ellipse2D.Double(sourceRect.x - frameRect.x - 12, frameRect.height - 21, 12, 12), false);
            } else {
                framePath.append(new Ellipse2D.Double(sourceRect.x - frameRect.x + sourceRect.width, frameRect.height - 6, 5, 5), false);
                framePath.append(new Ellipse2D.Double(sourceRect.x - frameRect.x + sourceRect.width + 1, frameRect.height - 21, 12, 12), false);
            }
        } else { // Bubble below speaker
            if(sourceRect.x > frameRect.x + frameRect.width / 2) { // tail at left
                framePath.append(new Ellipse2D.Double(sourceRect.x - frameRect.x - 4, 2, 5, 5), false);
                framePath.append(new Ellipse2D.Double(sourceRect.x - frameRect.x - 12, 10, 12, 12), false);
            } else {
                framePath.append(new Ellipse2D.Double(sourceRect.x - frameRect.x + sourceRect.width, 2, 5, 5), false);
                framePath.append(new Ellipse2D.Double(sourceRect.x - frameRect.x + sourceRect.width + 1, 10, 12, 12), false);
            }
        }
    }
    
    private void buildSpeechFrame() {
        Rectangle frameRect = getBounds();
        Rectangle sourceRect = ((RPGView)getParent()).getTileRectangle(speaker.getX(), speaker.getY());
        boolean isOnBottom = (frameRect.y < sourceRect.y);
        boolean tailAtLeft = (sourceRect.x > frameRect.x + frameRect.width / 2);
        
        framePath = new GeneralPath();
        
        // This whole business is greatly complicated by the fact that the text view inexplicably draws in a right-side-up
        // coordinate system. Yet the angle is still indexed as if it were all upide down. Or is that right-side up? Yes, the
        // angle is indexed as is fit and proper in math, but not in computer screens.
        
        framePath.append(arc(kBoundaryWidth, kBoundaryWidth + kTailHeight, kBoundaryWidth - 2, 180, -90), true);
        
        if(isBubble() && !isOnBottom) {
            if(tailAtLeft) {
                framePath.append(arc(sourceRect.x - frameRect.x - kTailHeight, kTailHeight / 2 + 2, kTailHeight / 2, 270, 90), true);
                framePath.append(arc(sourceRect.x - frameRect.x, kTailHeight / 2 + 2, kTailHeight / 2, 180, -90), true);
                framePath.append(arc(sourceRect.x - frameRect.x + kArcOffset, kTailHeight / 3 + 2, 2 * kTailHeight / 3, 120, 150), true);
            } else {
                framePath.append(arc(sourceRect.x - frameRect.x + sourceRect.width - kArcOffset, kTailHeight / 3 + 2, 2 * kTailHeight / 3, 270, 150), true);
                framePath.append(arc(sourceRect.x - frameRect.x + sourceRect.width, kTailHeight / 2 + 2, kTailHeight / 2, 90, -90), true);
                framePath.append(arc(sourceRect.x - frameRect.x + sourceRect.width + kTailHeight, kTailHeight / 2 + 2, kTailHeight / 2, 180, 90), true);
            }
        }
        
        framePath.append(arc(frameRect.width - kBoundaryWidth, kBoundaryWidth + kTailHeight, kBoundaryWidth - 2, 90, -90), true);
        framePath.append(arc(frameRect.width - kBoundaryWidth, frameRect.height - kBoundaryWidth - kTailHeight, kBoundaryWidth - 2, 0, -90), true);
        
        if(isBubble() && isOnBottom) {
            if(tailAtLeft) {
                framePath.append(arc(sourceRect.x - frameRect.x + kArcOffset, frameRect.height - (kTailHeight / 3 + 2), 2 * kTailHeight / 3, 90, 150), true);
                framePath.append(arc(sourceRect.x - frameRect.x, frameRect.height - (kTailHeight / 2 + 2), kTailHeight / 2, 270, -90), true);
                framePath.append(arc(sourceRect.x - frameRect.x - kTailHeight, frameRect.height - (kTailHeight / 2 + 2), kTailHeight / 2, 0, 90), true);
            } else {
                framePath.append(arc(sourceRect.x - frameRect.x + sourceRect.width + kTailHeight, frameRect.height - (kTailHeight / 2 + 2), kTailHeight / 2, 90, 90), true);
                framePath.append(arc(sourceRect.x - frameRect.x + sourceRect.width, frameRect.height - (kTailHeight / 2 + 2), kTailHeight / 2, 0, -90), true);
                framePath.append(arc(sourceRect.x - frameRect.x + sourceRect.width - kArcOffset, frameRect.height - (kTailHeight / 3 + 2), 2 * kTailHeight / 3, 300, 150), true);
            }
        }
        
        framePath.append(arc(kBoundaryWidth, frameRect.height - kBoundaryWidth - kTailHeight, kBoundaryWidth - 2, 270, -90), true);
        framePath.closePath();
    }
    
    public SpeechBubble(Script script, RPGView view) {
        super();
        speaker = script;
        
        script = script.getLevel().getScript(new Script(-2, Integer.parseInt(script.getArgument()), null, null));
        if(script == null) return;
        
        speaker.setArgument(script.getArgument());
        
        view.add(this);
        
        Graphics2D g = (Graphics2D)getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    
        AttributedString string = new AttributedString(script.getArgument());
        string.addAttribute(TextAttribute.FAMILY, "Serif");
        string.addAttribute(TextAttribute.SIZE, new Float(20f));
        string.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
    
        measurer = new LineBreakMeasurer(string.getIterator(), g.getFontRenderContext());
        
        if(isBubble()) {
            placeBubble();
        } else {
            placePanel();
        }
        
        if(speaker.getAction().indexOf("Speech") != -1) {
            buildSpeechFrame();
        } else if(speaker.getAction().indexOf("Thought") != -1) {
            buildThoughtFrame();
        } else {
            buildSignFrame();
        }
         
        Map.getMap().getKey().pushTarget(this);
    }
    
    public void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D)graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
        g.setColor(bgColor);
        g.fill(framePath);
        g.setStroke(new BasicStroke(3f));
        g.setColor(Color.WHITE);
        g.draw(framePath);
        
        float y = (float)(kBoundaryHeight + kTailHeight);
        
        measurer.setPosition(0);
        while (measurer.getPosition() < speaker.getArgument().length()) {
            TextLayout layout = measurer.nextLayout(width);
            
            y += layout.getAscent();
            layout.draw(g, (float)kBoundaryWidth, y);
            y += layout.getDescent() + layout.getLeading();
        }
    }
    
    public static boolean supportsType(String type) {
        String frameType = null;
        int i = type.indexOf("Bubble");
        if(i != -1) {
            frameType = type.substring(0, i);
        } else {
            i = type.indexOf("Panel");
            if(i != -1) {
                frameType = type.substring(0, i);
            } else {
                return false;
            }
        }
        return frameType.equals("Speech") || frameType.equals("Thought") || frameType.equals("Sign");
    }
    
    public void keySpacePressed() {
        Map.getMap().getKey().popTarget(this);
        Container c = getParent();
        c.remove(this);
        c.repaint();
    }
}
