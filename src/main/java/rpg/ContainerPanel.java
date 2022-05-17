package rpg;

import java.util.ArrayList;
import java.awt.Rectangle;

/**
 * Write a description of class ContainerPanel here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class ContainerPanel {
    ItemContainer container;
    
    public void doLayout(MenuPanel panel) {
        ArrayList items = container.getItems();
        panel.clear();
        panel.addString("Take items:");
        for(int i = 0; i < items.size(); i++) {
            Item item = (Item)items.get(i);
            panel.addLink(item, item.getDescription(), "\n- ");
        }
        panel.addLink("Done", "Done", "\n\n> ");
    }
    
    public void doLink(MenuPanel panel, Object link) {
        if(link.equals("Done")) {
            panel.goodbye();
        } else if(link instanceof Item) {
            Item item = (Item)link;
            Creature player = Map.getMap().getPlayer();
            
            container.removeItem(item.getName(), -1);
            
            if(item.getName().equals("Gold")) {
                player.setGold(player.getGold() + item.getCount());
            } else {
                player.getContainer().addItem(item);
            }
            panel.doMenuPanelLayout();
        }
    }
    
    public ContainerPanel(Script script, RPGView view) {
        Level level = script.getLevel();
        script = new Script(script.getX(), script.getY(), script.getArgument(), null);
        int i = 0;
        Script s = level.getScript(i++, script);
        while(!(s instanceof VisibleObject) || ((VisibleObject)s).getContainer() == null) {
            s = level.getScript(i++, script);
            if(s == null) return;
        }
        
        container = ((VisibleObject)s).getContainer();
        
        Rectangle bounds = view.getBounds();
        bounds.setBounds(bounds.x + 40, bounds.y + 40, bounds.width - 80, bounds.height - 80);
        MenuPanel panel = new MenuPanel(view, bounds);
        panel.setDataSource(this);
        panel.doMenuPanelLayout();
    }
}
