package rpg;

import java.util.ArrayList;
import java.util.HashMap;
import java.awt.Rectangle;

/**
 * Write a description of class ConversationPanel here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class ConversationPanel implements XMLParser.ParseListener {
    private Creature player, other;
    private HashMap nodes;
    private XMLParser parser;
    
    private static class ConversationOption {
        public String link, question, sign;
        public int index;
        
        public ConversationOption(String link, int index, String sign) {
            this.link = link;
            this.index = index;
            question = "";
            this.sign = sign;
            
            if(link == null) this.link = "root";
            if(sign == null) this.sign = "";
        }
    }
    
    private static class ConversationItem {
        public String name;
        public int count;
        
        public ConversationItem(String string) {
            int i = string.indexOf(":");
            if(i == -1) {
                count = 1;
                name = string;
            } else {
                name = string.substring(0, i);
                count = Integer.parseInt(string.substring(i + 1));
            }
        }
        
        public static ArrayList parse(String items) {
            ArrayList list = new ArrayList();
            if(items == null) return list;
            
            int i;
            while((i = items.indexOf(",")) != -1) {
                list.add(new ConversationItem(items.substring(0, i)));
                items = items.substring(i + 1);
            }
            list.add(new ConversationItem(items));
            return list;
        }
    }
    
    private class ConversationNode implements XMLParser.ParseListener {
        public String name;
        public ArrayList give;
        public ArrayList take;
        public ArrayList options;
        public double priceMultiplier;
        public ConversationOption parsingOption;
        public String speech;
        public String type;
        
        public ConversationNode(HashMap attributes) {
            name = (String)attributes.get("name");
            if(name == null) name = "root";
            name = name.toLowerCase();
            
            type = (String)attributes.get("type");
            if(type == null) type = "normal";
            type = type.toLowerCase();
            
            if(type.equals("buy") || type.equals("sell")) {
                String multiplier = (String)attributes.get("multiplier");
                if(multiplier == null) {
                    if(type.equals("buy")) priceMultiplier = 1;
                    else priceMultiplier = .5;
                } else {
                    priceMultiplier = Double.parseDouble(multiplier);
                }
            }
            
            give = ConversationItem.parse((String)attributes.get("give"));
            take = ConversationItem.parse((String)attributes.get("take"));
            
            options = new ArrayList();
            speech = "";
            parsingOption = null;
            
            parser.pushParser(this);
        }
        
        public boolean transfer(Creature from, Creature to, ArrayList items, boolean doIt) {
            ItemContainer myItems = from.getContainer();
            ItemContainer yourItems = to.getContainer();
            
            for(int i = 0; i < items.size(); i++) {
                ConversationItem ci = (ConversationItem)items.get(i);
                if(ci.name.equals("Gold")) {
                    if(from.getGold() < ci.count && from.getGold() != -1) return true;
                    if(doIt) {
                         if(ci.count == -1) {
                            to.setGold(to.getGold() + from.getGold());
                            from.setGold(0);
                        } else {
                            to.setGold(to.getGold() + ci.count);
                            from.setGold(from.getGold() - ci.count);
                        }
                    }
                } else if(ci.name == "Experience") {
                    if(doIt) {
                        if(to.isPlayer()) {
                            to.setExperience(to.getExperience() + ci.count);
                        } else if(from.isPlayer()) {
                            from.setExperience(from.getExperience() - ci.count);
                        }
                    }
                } else if(!doIt || ci.count == 0) {
                    Item item = myItems.getItem(ci.name);
                    if(item == null || (item.getCount() != -1 && item.getCount() < ci.count)) return true;
                } else {
                    Item item = myItems.removeItem(ci.name, ci.count);
                    if(item == null) return true;
                    yourItems.addItem(item);
                }
            }
            return false;
        }
        
        public boolean isBlocked() {
            return transfer(player, other, take, false) || transfer(other, player, give, false);
        }
        
        public void doLayout(MenuPanel panel) {
            panel.clear();
            
            transfer(player, other, take, true);
            transfer(other, player, give, true);
            
            boolean shopped = false, openOption = false;
            
            int index = 0;
            for(int i = 0; i < options.size(); i++) {
                ConversationOption co = (ConversationOption)options.get(i);
                panel.addString(speech.substring(index, co.index));
                index = co.index;
                
                if(index == speech.length() && !shopped) {
                    addShop(panel);
                    shopped = true;
                }
                
                ConversationNode link = (ConversationNode)nodes.get(co.link);
                if(link == null || link.isBlocked()) {
                    if(co.sign.equals("")) {
                        // If this is an inline link, just have it appear not as a link
                        // Otherwise, don't even allow it to show up
                        panel.addString(co.question);
                    }
                } else {
                    panel.addLink(co.link, co.question, co.sign);
                    openOption = true;
                }
            }

            panel.addString(speech.substring(index));
            
            if(!shopped) {
                addShop(panel);
            }
            String goodbye = "Goodbye";
            if(type.equals("end")) {
                goodbye = "(The conversation ends)";
            } else if(type.equals("attack")) {
                other.setFriendly(false);
                goodbye = "(Prepare to fight)";
            } else if(type.equals("die")) {
                other.die();
                goodbye = "(The conversation ends)";
            } else if(!openOption) { // Dead end in conversation
                panel.addLink("root", "I have another question.");
            }
            
            if(type.equals("inn")) {
                player.heal();
                player.restoreMagic();
            }
    
            panel.addLink("goodbye", goodbye);
        }
        
        private void addShop(MenuPanel panel) {
            if(type.equals("buy")) {
                panel.addString("\n\nPurchase items:");
                addShop(panel, other.getContainer().getItems(), false);
            } else if(type.equals("sell")) {
                panel.addString("\n\nSell items:");
                addShop(panel, player.getContainer().getItems(), true);
            } else {
                return;
            }
            String storeGold = (other.getGold() == -1 ? "" : "\nStore gold: " + other.getGold());
            panel.addString("\n\nGold remaining: ");
            panel.addIndicator(player.getGold() + storeGold);
            
            changedSelection(panel, panel.getSelectedLink());
        }
        
        public void changedSelection(MenuPanel panel, Object link) {
            if(link instanceof Item) {
                int gold = (int)(((Item)link).goldValue() * priceMultiplier);
                String storeGold = (other.getGold() == -1 ? "" : "\nStore gold: " + other.getGold());
                if(type.equals("buy")) {
                    panel.setIndicator(player.getGold() + " (- " + gold + ")" + storeGold);
                } else {
                    panel.setIndicator(player.getGold() + " (+ " + gold + ")" + storeGold);
                }
            } else if(type.equals("buy") || type.equals("sell")) {
                panel.setIndicator("" + player.getGold());
            }
        }
        
        private void addShop(MenuPanel panel, ArrayList items, boolean counts) {
            int gold = -1;
            for(int i = 0; i < items.size(); i++) {
                Item item = (Item)items.get(i);
                if(item.isRealItem() && item.goldValue() > 0) {
                    if(gold == -1) gold = (int)(item.goldValue() * priceMultiplier);
                    
                    panel.addLink(item, (counts ? item.getDescription() : item.getName()), "\n- ");
                }
            }
        }
        
        public void doLink(MenuPanel panel, Object link) {
            if(link.equals("goodbye")) {
                panel.goodbye();
            } else if(link instanceof Item) {
                transactBusiness(panel, (Item)link);
            } else if(link instanceof String) {
                ConversationNode node = (ConversationNode)nodes.get(link);
                if(node == null) return;
                
                panel.setDataSource(node);
                panel.doMenuPanelLayout();
            }
        }
        
        private void transactBusiness(MenuPanel panel, Item item) {
            if(type.equals("buy")) {
                transactBusiness(panel, item, other, player);
            } else if(type.equals("sell")) {
                transactBusiness(panel, item, player, other);
            }
        }
        
        private void transactBusiness(MenuPanel panel, Item item, Creature from, Creature to) {
            int gold = (int)(item.goldValue() * priceMultiplier);
            
            // Enough money to make the trade?
            if(to.getGold() != -1 && to.getGold() < gold) {
                return;
            }
            
            Item moving = from.getContainer().removeItem(item.getName(), 1);
            
            from.setGold(from.getGold() + gold);
            to.setGold(to.getGold() - gold);
            to.getContainer().addItem(moving);
            
            if(from.getContainer().getItem(item.getName()) == null) {
                panel.doMenuPanelLayout();
            } else {
                String storeGold = (other.getGold() == -1 ? "" : "\nStore gold: " + other.getGold());
                if(from.isPlayer()) {
                    panel.setLinkText(item, item.getDescription());
                    panel.setIndicator(player.getGold() + " (+ " + gold + ")" + storeGold);
                } else {
                    panel.setIndicator(player.getGold() + " (- " + gold + ")" + storeGold);
                }
            }
        }
        
        public void startElement(String name, HashMap attributes) {
            if(name.equals("option")) {
                parsingOption = new ConversationOption((String)attributes.get("link"), speech.length(), (String)attributes.get("sign"));
                options.add(parsingOption);
            }
        }
        
        public void readText(String text) {
            if(parsingOption != null) {
                parsingOption.question += text;
            } else {
                speech += text;
            }
        }
        
        public void endElement(String name) {
            if(name.equals("option") && parsingOption != null) {
                if(parsingOption.question.equals("")) {
                    parsingOption.question = parsingOption.link;
                } else if(parsingOption.sign.equals("")) {
                    parsingOption.sign = "\n> ";
                }
                parsingOption.link = parsingOption.link.toLowerCase();
                parsingOption = null;
            } else if(name.equals("node")) {
                parser.popParser();
            } else if(name.equals("br")) {
                if(parsingOption != null) parsingOption.question += "\n";
                else speech += "\n";
            }
        }
    }
    
    public void startElement(String name, HashMap attributes) {
        if(name.equals("node")) {
            ConversationNode node = new ConversationNode(attributes);
            if(!nodes.containsKey(node.name)) nodes.put(node.name, node);
        }
    }
    
    public void readText(String text) {}
    public void endElement(String name) {}

    public ConversationPanel(Script conversation, RPGView view) {
        player = Map.getMap().getPlayer();
        other = (Creature)player.getLevel().getScript(new Script(conversation.getX(), conversation.getY(), "Creature", null));
        nodes = new HashMap();
        
        conversation = player.getLevel().getScript(new Script(-2, Integer.parseInt(conversation.getArgument()), "Conversation", null));
        parser = new XMLParser(conversation.getArgument(), this);
        parser.parse();
        
        ConversationNode root = (ConversationNode)nodes.get("root");
        
        Rectangle bounds = view.getBounds();
        bounds.setBounds(bounds.x + 40, bounds.y + 40, bounds.width - 80, bounds.height - 80);
        MenuPanel panel = new MenuPanel(view, bounds);
        panel.setDataSource(root);
        panel.doMenuPanelLayout();
    }
}
