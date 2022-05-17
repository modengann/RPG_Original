package rpg;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;

/**
 * Write a description of class RPGMenu here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class RPGMenu {
    private Creature player, compareCreature;
    private String currentTag;
    private MenuPanel mainPanel, sidePanel;
    private RPGView view;
    private Rectangle bounds;
    private SpellChoice spells, spell;
    
    /*
     * A SpellChoice is a menu item within the Spells system. It has a
     * name, and may contain a list of submenu choices. It also has a 
     * link back to the menu it is contained within. It can be told
     * to layout a menu panel for itself, using SpellChoice objects as
     * option values.
     */
    private class SpellChoice {
    	private String name;
    	private ArrayList choices;
    	private SpellChoice parent;
    	
    	public SpellChoice(String name, SpellChoice parent) {
    		this.name = name;
    		this.parent = parent;
    		if(parent != null) parent.addChoice(this);
    		choices = null;
    	}
    	
    	public SpellChoice getSpell(String name) {
    		if(this.name.equals(name)) return this;
    		else if(choices == null) return null;
    		
    		for(int i = 0; i < choices.size(); i++) {
    			SpellChoice s = ((SpellChoice)choices.get(i)).getSpell(name);
    			if(s != null) return s;
    		}
    		return null;
    	}
    	
    	public String toString() {
    		return "SpellChoice (" + (parent == null ? "" : parent.name + ":") + name + ")"
    			+ (choices == null ? "" : " - " + choices.size() + " choices");
    	}
    	
		private void addChoice(SpellChoice choice) {
    		if(choices == null) choices = new ArrayList();
    		choices.add(choice);
    	}
    	
    	public boolean isSpell() {
    		return choices == null;
    	}
    	
		public SpellChoice getParent() {
			return parent;
		}
		
		public void doLayout(MenuPanel panel) {
			panel.addString(name + ":\n");
			
			char c = 'a';
			for(int i = 0; choices != null && i < choices.size(); i++) {
				SpellChoice choice = (SpellChoice)choices.get(i);
				panel.addLink(choice, choice.name, "\n" + c + ". ", "" + c);
				
				if(c == 'z') c = 'A';
				else c++;
			}
			if(parent == null) {
				panel.addLink("Root", "Main menu", "\n\n> ", "escape");
			} else {
				panel.addLink(parent, "back to " + parent.name, "\n\n> ", "escape");
			}
		}
		
		public String method() {
			String method = "cast";
			String s = name;
			while(s.length() != 0) {
				int i = s.indexOf(" ");
				String w;
				if(i == -1) {
					w = s;
					s = "";
				} else {
					w = s.substring(0, i);
					s = s.substring(i + 1);
				}
				w = w.substring(0, 1).toUpperCase() + w.substring(1).toLowerCase();
				method += w;
			}
			return method;
		}
    }
    
    private void buildChoices() {
    	String string = null;
    	spells = null;
    	spell = null;
    	
    	try {
			string = (String)player.getClass().getMethod("getSpells", null).invoke(player, null);
    	} catch(Exception e) {
    		return;
    	}
    	
    	spells = new SpellChoice("Spells", null);
		SpellChoice container = spells;
		spell = spells;
		
    	// Read the next name, up to a comma, semicolon, or colon, and make a
    	// SpellChoice. Add it to the current container.
    	// If it is a colon at the end of this name, make this one the current container.
    	// If it is a semicolon at the end, break out of this container.
    	while(!string.equals("")) {
    		String c = "", name = null;
    		
    		while(string.substring(0, 1).equals(" ")) string = string.substring(1);
    		
    		for(int i = 0; i < string.length(); i++) {
				c = string.substring(i, i + 1);
    			if(c.equals(":") || c.equals(";") || c.equals(",")) {
    				name = string.substring(0, i);
    				string = string.substring(i + 1);
    				break;
    			}
    		}
    		if(name == null) {
    			name = string;
    			string = "";
    		}
    		if(!name.equals("")) {
				SpellChoice choice = new SpellChoice(name, container);
				if(c.equals(":")) {
					container = choice;
				}
    		}
    		if(c.equals(";")) {
    			container = container.getParent();
    			if(container == null) {
    				throw new RuntimeException("Bad format for spells list.");
    			}
    		}
    	}
    }
    
    public RPGMenu() {
        player = Map.getMap().getPlayer();
        currentTag = "Root";
        buildChoices();
    }
    
    public void runInView(RPGView view) {
        this.view = view;
        
        bounds = view.getBounds();
        bounds.setBounds(bounds.x + 40, bounds.y + 40, bounds.width - 80, bounds.height - 80);
        mainPanel = new MenuPanel(view, bounds);
        
        mainPanel.setDataSource(this);
        mainPanel.doMenuPanelLayout();
        
        sidePanel = null;
    }
    
    // Return whether the panel had to be killed
    private boolean killSidePanel() {
        if(compareCreature != null) compareCreature = null;
        
        if(sidePanel != null) {
            sidePanel.goodbye();
            sidePanel = null;
            return true;
        } else {
            return false;
        }
    }
    
    public void layoutInventory(MenuPanel panel) {
        if(panel == mainPanel) {
            ArrayList items = player.getContainer().getItems();
            int i;
            panel.clear();
            panel.addString("Inventory");
            for(i = 0; i < items.size(); i++) {
                Item item = (Item)items.get(i);
                if(item.isRealItem()) {
                    panel.addLink(item, item.getDescription(), "\n- ");
                }
            }
            panel.addLink("Root", "Main Menu", "\n\n> ");
        } else {
            Item item = (Item)mainPanel.getSelectedLink();
            boolean isPriceless = (item.goldValue() == -1);
            
            panel.clear();
            panel.addIndicator(item.getDescription());
            
            if(isPriceless) {
                panel.addString("\nSpecial item");
            } else {
                panel.addString("\nValue: " + item.goldValue() + " gold");
            }
            
            if(item.getSlot() != -1) panel.addLink("Equip", "Equip");
            if(item.canUse()) panel.addLink("Use", "Use");
            
            if(!isPriceless) {
                panel.addLink("Drop", "Drop");
                if(item.getCount() > 1) {
                    panel.addLink("Drop all", "Drop all");
                }
            }
            panel.addLink("Done", "Done");
        }
    }
    
    public void linkFromInventory(MenuPanel panel, Object link) {
        if(panel == mainPanel) {
            killSidePanel();
            Rectangle frame = panel.getView();
            frame.width /= 2;
            frame.x += frame.width;
            frame.height /= 2;
            
            sidePanel = new MenuPanel(panel, frame);
            sidePanel.setDataSource(this);
            sidePanel.doMenuPanelLayout();
        } else {
            Item item = (Item)mainPanel.getSelectedLink();
            if(link.equals("Done")) {
                killSidePanel();
            } else if(link.equals("Equip")) {
                player.equipItem(item, item.getSlot());
            } else if(link.equals("Use")) {
                player.useItem(item);
            } else if(link.equals("Drop")) {
                player.dropItem(item.getName(), 1);
            } else if(link.equals("Drop all")) {
                player.dropItem(item.getName(), -1);
            }
            
            item = player.getContainer().getItem(item.getName());
            if(item == null) {
                killSidePanel();
                mainPanel.doMenuPanelLayout();
            } else {
                mainPanel.setLinkText(item, item.getDescription());
                if(sidePanel != null) sidePanel.setIndicator(item.getDescription());
            }
        }
    }
    
    public void layoutEquip(MenuPanel panel) {
        if(panel == sidePanel) {
            int slot = ((Integer)mainPanel.getSelectedLink()).intValue();
            ArrayList items = player.getContainer().getItems();
            
            panel.clear();
            panel.addString("Equip " + player.getSlots()[slot] + ": ");
            for(int i = 0; i < items.size(); i++) {
                Item item = (Item)items.get(i);
                if(item.getSlot() == slot) {
                    panel.addLink(item, item.getName(), "\n- ");
                }
            }
            Item item = (Item)player.getItemInSlot(slot);
            if(item != null) {
                panel.addLink("Empty", "(Empty)", "\n- ");
                panel.addLink(item, "Cancel", "\n\n> ");
            } else {
                panel.addLink("Empty", "Cancel", "\n\n> ");
            }
            
            changeEquipSelection(panel, panel.getSelectedLink());
        } else { // main panel
            String[] slots = player.getSlots();
            panel.clear();
            panel.addString("Equipped:");
            for(int i = 0; i < slots.length; i++) {
                panel.addString("\n" + slots[i] + ": ");
                Item item = (Item)player.getItemInSlot(i);
                if(item == null) {
                    panel.addLink(new Integer(i), "(Empty)", "");
                } else {
                    panel.addLink(new Integer(i), item.getName(), "");
                }
            }
            panel.addLink("Root", "Main Menu", "\n\n> ");
            panel.addString("\n\n");
            panel.addIndicator(player.getDescription());
        }
    }
    
    public void changeEquipSelection(MenuPanel panel, Object link) {
        if(panel != sidePanel) return;
        
        int slot = ((Integer)mainPanel.getSelectedLink()).intValue();
        
        if(link instanceof Item) {
            player.equipItem((Item)link, slot);
            mainPanel.setLinkText(mainPanel.getSelectedLink(), ((Item)link).getName());
        } else {
            player.equipItem(null, slot);
            mainPanel.setLinkText(mainPanel.getSelectedLink(), "(Empty)");
        }
        
        mainPanel.setIndicator(player.compareToCreature(compareCreature));
    }
    
    public void linkFromEquip(MenuPanel panel, Object link) {
        if(panel == sidePanel) {
            mainPanel.setIndicator(player.getDescription());
            killSidePanel();
        } else {
            killSidePanel();
            Rectangle frame = panel.getView();
            frame.width /= 2;
            frame.x += frame.width;
            
            player.updateArgument();
            compareCreature = new Creature(player);
            
            sidePanel = new MenuPanel(panel, frame);
            sidePanel.setDataSource(this);
            sidePanel.doMenuPanelLayout();
        }
    }
    
	public void layoutSave(MenuPanel panel) {
		RPGApp.getApp().save();
	}
    
	public void layoutSpells(MenuPanel panel) {
		if(spell == null) return;
		panel.clear();
		spell.doLayout(panel);
	}
    
	public void linkFromSpells(MenuPanel panel, Object link) {
		if(!(link instanceof SpellChoice)) {
			currentTag = "Root";
			panel.doMenuPanelLayout();
		}
		spell = (SpellChoice)link;
		
		if(spell.isSpell()) {
			panel.goodbye();
			try {
				player.getClass().getMethod(spell.method(), new Class[] {Creature.class});
				new Targeter(view, spell.method());
			} catch(Exception e) {
				// No method for that spell implemented
			}
		} else {
			panel.doMenuPanelLayout();
		}
	}
	
    public void layoutOpen(MenuPanel panel) {
        layoutExit(panel);
        RPGApp.getApp().open();
    }
    
	public void layoutExit(MenuPanel panel) {
		killSidePanel();
		panel.goodbye();
	}
    
	public void layoutLevelUp(MenuPanel panel) {
		panel.addString("You are now level " + player.getExperienceLevel() + 
			"!\n\nEach time you level up, you may choose to increase one of your primary stats:");
			
		panel.addLink("Strength", "Strength", "\n\n> ");
		panel.addString(" (" + player.getStrength() + ")");
		
		panel.addLink("Dexterity", "Dexterity", "\n\n> ");
		panel.addString(" (" + player.getDexterity() + ")");
		
		panel.addLink("Intelligence", "Intelligence", "\n\n> ");
		panel.addString(" (" + player.getIntelligence() + ")");
		
		panel.addString("\n\nStrength affects how mny hit points you have, and how much damage you do. " +
							"Dexterity affects how likely you are to hit an enemy or block their attack. " + 
							"Intelligence makes all your spells more effective, and increases your spell points.");
	}
	
	public void linkFromLevelUp(MenuPanel panel, Object link) {
		if(link.equals("Strength")) {
			player.setStrength(player.getStrength() + 1);
		} else if(link.equals("Dexterity")) {
			player.setDexterity(player.getDexterity() + 1);
		} else if(link.equals("Intelligence")) {
			player.setIntelligence(player.getIntelligence() + 1);
		}
		panel.goodbye();
	}
    
    public void layoutRoot(MenuPanel panel) {
        panel.layoutXML("<menu id=\"Root\">Main Menu:" + 
            "<option link=\"Inventory\"/>" + "<option link=\"Equip\"/>" + 
            (spells == null ? "" : "<option link=\"Spells\"/>") + 
            (RPGApp.getApp() == null ? "" : "<option link=\"Save\"/><option link=\"Open\"/>") + 
            "<option link=\"Exit\"/>\n\n" + 
            player.getShortDescription() + "\n\n" + 
            DateFormat.getInstance().format(new Date(System.currentTimeMillis())) + "</menu>");
    }
    
    public void doLayout(MenuPanel panel) {
        try {
            getClass().getMethod("layout" + currentTag, new Class[] {MenuPanel.class}).invoke(this, new Object[] {panel});
        } catch(Exception e) {
            e.printStackTrace();
            currentTag = "Root";
            layoutRoot(panel);
        }
    }
    
    public void doLink(MenuPanel panel, Object link) {
        if(("Root").equals(link)) {
            currentTag = "Root";
            killSidePanel();
            panel.doMenuPanelLayout();
        } else if(currentTag.equals("Root")) {
            currentTag = (String)link;
            panel.doMenuPanelLayout();
        } else {
            try {
                getClass().getMethod("linkFrom" + currentTag, new Class[] {MenuPanel.class, Object.class}).invoke(this, new Object[] {panel, link});
            } catch(Exception e) {
            	e.printStackTrace();
            }
        }
    }
    
    public void changedSelection(MenuPanel panel, Object link) {
        try {
            getClass().getMethod("change" + currentTag + "Selection", new Class[] {MenuPanel.class, Object.class}).invoke(this, new Object[] {panel, link});
        } catch(Exception e) {
        }
    }
    
    public void doMenuKey(MenuPanel panel) {
        if(killSidePanel()) {
            return;
        } else if(currentTag.equals("Root")) {
            mainPanel.goodbye();
        } else {
            currentTag = "Root";
            mainPanel.doMenuPanelLayout();
        }
    }
    
    public void setPage(String page) {
        currentTag = page;
        try {
			getClass().getMethod("layout" + currentTag, new Class[] {MenuPanel.class});
        } catch(Exception e) {
        	if(spells == null) {
        		currentTag = "Root";
        	} else {
				spell = spells.getSpell(page);
				if(spell == null) currentTag = "Root";
				else currentTag = "Spells";
        	}
        }
    }
}