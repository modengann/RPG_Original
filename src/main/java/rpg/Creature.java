package rpg;

import java.util.ArrayList;


/**
 * A Creature represents any living thing in the RPG world. Creatures have statistics describing their health and fighting
 * ability, and can be asked to fight each other. They can also have information on what conversation will be acivated when
 * the player talks to them.
 * 
 * In part 4, you will write the code that controls the player and makes it possible for him to move around the map. You may
 * also choose to make enemies move around in the same way.
 * 
 * It would be a good idea to look thoroughly through the interface of this class so that you have a good idea of what things
 * you can tell a creature to do. For example, talking to or attacking a creature can be accomplished with a simple call to
 * existing methods.
 * 
 * @author (your name)
 * @version (a version number or a date)
 */
public class Creature extends VisibleObject {
    private int level, strength, dexterity, intelligence, hits, magic, maxHits, maxMagic, armor, weapon, range, experience, gold, conversation;
    private static boolean isAction;
    private boolean friendly;
    private String direction;
    private Item[] equipped = null;
    
    private static final String[] slots = {"Weapon", "Ammunition", "Shield", "Body", "Head", "Hands", "Feet", "Special"};
    
    /**
     * Create a new Creature based on the given script. When treated as a Script, this Creature will have all the same
     * parameters (x, y, action, argument) as the given script; however, by making it a Creature we give it additional
     * capabilities such as moving around and being drawn on the screen.
     */
    public Creature(Script script) {
        super(script);
    }
    
    public int getExperienceLevel() {
    	return level;
    }
    
	public int getStrength() {
		return strength;
	}
    
	public int getDexterity() {
		return dexterity;
	}
    
	public int getIntelligence() {
		return intelligence;
	}
	
	public void setStrength(int s) {
		strength = s;
		int mh = (int)(30 + level * (6 + strength * .5));
		hits += mh - maxHits;
		maxHits = mh;
	}
	
	public void setDexterity(int d) {
		dexterity = d;
	}
	
	public void setIntelligence(int i) {
		intelligence = i;
		int mm = (int)(.5 * intelligence * (3 + level));
		magic += mm - maxMagic;
		maxMagic = mm;
	}
    
    public void loadInformation(Tile tile) {
        super.loadInformation(tile);
        
        level = tile.getParameter("l");
        strength = tile.getParameter("s");
        dexterity = tile.getParameter("d");
        intelligence = tile.getParameter("j");
        hits = tile.getParameter("h");
        magic = tile.getParameter("m");
        maxHits = tile.getParameter("H");
        maxMagic = tile.getParameter("M");
        weapon = tile.getParameter("w");
        armor = tile.getParameter("a");
        range = tile.getParameter("r");
        experience = tile.getParameter("e");
        gold = tile.getParameter("g");
        
        friendly = tile.getFlag("F");
        conversation = tile.getParameter("C");
    }
    
    public void updateArgument() {
        Tile tile = getTile();
        String arg;
        if(tile.getNumber() != -1) {
            // If the tile I belong to is a registered type, not a made-up type, just note what is
            // different between me and my tile.
            arg = ":t" + tile.getNumber();
            if(!getName().equals(tile.getName())) arg = getName() + arg;
            if(level != tile.getParameter("l")) arg += "l" + level;
            if(strength != tile.getParameter("s")) arg += "s" + strength;
            if(dexterity != tile.getParameter("d")) arg += "d" + dexterity;
            if(intelligence != tile.getParameter("j")) arg += "j" + intelligence;
            if(hits != tile.getParameter("h")) arg += "h" + hits;
            if(magic != tile.getParameter("m")) arg += "m" + magic;
            if(maxHits != tile.getParameter("H")) arg += "H" + maxHits;
            if(maxMagic != tile.getParameter("M")) arg += "M" + maxMagic;
            if(weapon != tile.getParameter("w")) arg += "w" + weapon;
            if(armor != tile.getParameter("a")) arg += "a" + armor;
            if(range != tile.getParameter("r")) arg += "r" + range;
            if(gold != tile.getParameter("g")) arg += "g" + gold;
            if(experience != tile.getParameter("e")) arg += "e" + experience;
            if(conversation != tile.getParameter("C")) arg += "C" + conversation;
            if(friendly != tile.getFlag("F")) arg += "F";
        } else {
            arg = getName() + ":l" + level + "s" + strength + "d" + dexterity + "j" + intelligence + "h" + hits + "m" + magic
                 + "H" + maxHits + "M" + maxMagic + "w" + weapon + "a" + armor + "r" + range + "g" + gold + "e" + experience;
            if(conversation != -1) arg += "C" + conversation;
            if(friendly) arg += "F";
        }
        setArgument(arg);
    }
    
    /**
     * Add the given item to your container, if you have a container. Also takes care of
     * equipping it if the item has a special count to indicate that it should be
     * equipped.
     * @param item Item to take
     */
    public void takeItem(Item item) {
		ItemContainer container = getContainer();
		if(container != null) {
			if(item.getCount() <= -10) {
				int slot = -10 - item.getCount();
				item.setCount(1);
				container.addItem(item);
				equipItem(item, slot);
			} else {
				container.addItem(item);
			}
		}
    }
    
	public String toString() {
		if(equipped == null) {
			return super.toString();
		} else {
			for(int i = 0; i < equipped.length; i++) {
				if(equipped[i] != null) {
					addEffects(equipped[i], -1);
					equipped[i].setCount(-10 - i);
					equipped[i].move(getX(), getY());
				}
			}
			String s = super.toString();
			for(int i = 0; i < equipped.length; i++) {
				if(equipped[i] != null) s += "\n" + equipped[i].toString();
			}
			return s;
		}
	}
    
    private void addEffects(Item item, int multiplier) {
        if(item.getTile().getParameter("s") != -1) strength += multiplier * item.getTile().getParameter("s");
        if(item.getTile().getParameter("d") != -1) dexterity += multiplier * item.getTile().getParameter("d");
        if(item.getTile().getParameter("j") != -1) intelligence += multiplier * item.getTile().getParameter("j");
        if(item.getTile().getParameter("H") != -1) {
            maxHits += multiplier * item.getTile().getParameter("H");
            hits += multiplier * item.getTile().getParameter("H");
        }
        if(item.getTile().getParameter("M") != -1) {
            maxMagic += multiplier * item.getTile().getParameter("M");
            magic += multiplier * item.getTile().getParameter("M");
        }
        if(item.getTile().getParameter("h") != -1) {
            hits += multiplier * item.getTile().getParameter("h");
            if(hits <= 0) hits = 1;
            else if(hits > maxHits) hits = maxHits;
        }
        if(item.getTile().getParameter("m") != -1) {
            magic += multiplier * item.getTile().getParameter("m");
            if(magic < 0) magic = 0;
            else if(magic > maxMagic) magic = maxMagic;
        }
        if(item.getTile().getParameter("w") != -1) weapon += multiplier * item.getTile().getParameter("w");
        if(item.getTile().getParameter("a") != -1) armor += multiplier * item.getTile().getParameter("a");
        if(item.getTile().getParameter("r") != -1) range += multiplier * item.getTile().getParameter("r");
    }
    
    /**
     * Equip the given item in the given slot. Equipping null leaves that slot empty.
     */
    public void equipItem(Item item, int slot) {
        if(slot < 0 || slot >= slots.length) return;
        
        if(equipped == null) equipped = new Item[slots.length];
        
        Item wasEquipped = equipped[slot];
        equipped[slot] = item;
        
        if(item != null) addEffects(item, 1);
        if(wasEquipped != null) addEffects(wasEquipped, -1);
        
        ItemContainer container = getContainer();
        if(container != null) {
            if(wasEquipped != null) container.addItem(wasEquipped);
            if(item != null) container.removeItem(item.getName(), 1);
        }
    }
    
    /**
     * Use the given item. This adds to this creature's stats any creature stats that the item has.
     */
    public void useItem(Item item) {
        if(!item.getTile().getFlag("U")) return;
        addEffects(item, 1);
        ItemContainer container = getContainer();
        if(container != null) container.removeItem(item.getName(), 1);
    }
    
    /**
     * Get the item this creature has equipped in its nth slot
     */
    public Item getItemInSlot(int slot) {
        if(equipped == null || slot >= equipped.length) return null;
        return equipped[slot];
    }
    
    /**
     * List the possible slots items can be equipped in (weapon, shoes, head, etc)
     */
    public String[] getSlots() {
        return slots;
    }
    
    /**
     * Calling this method will make this creature say anything that it has to say.
     * This means either creating a speech bubble or launching into a full conversation.
     */
    public void talk() {
        if(conversation == -1) return;
        
        Script script = getLevel().getScript(new Script(-2, conversation, null, null));
        if(script == null) return;
        
        if(script.getAction().equals("Conversation")) {
            getLevel().addScript(new Script(getX(), getY(), "ConversationPanel", Integer.toString(conversation)));
        } else if(SpeechBubble.supportsType(script.getAction())) {
            getLevel().addScript(new Script(getX(), getY(), script.getAction(), Integer.toString(conversation)));
        }
    }
    
    /**
     * Is this creature friendly?
     * @return true if this creature is an ally, false if it is an enemy
     */
    public boolean isFriendly() {
        return friendly;
    }
    
    /**
     * Set whether or not this creature is friendly.
     * @param f whether this creature is now friendly
     */
    public void setFriendly(boolean f) {
        friendly = f;
    }
    
    /**
     * Is this creature the player?
     * @return true if this creature is the player, false otherwise
     */
    public boolean isPlayer() {
        return Map.getMap().getPlayer() == this;
    }
    
    /**
     * Attack the specified creature. Returns <code>true</code> if this creature was able to make the
     * attack, regardless of whether that attack succeeded; returns <code>false</code> if that creature 
     * was not in range of any of this creature's weapons.
     * @param other Creature to attack
     * @return <code>true</code> if the attack occurred, <code>false</code> if the target was out of range
     */
    public boolean attack(Creature other) {
        int dx = getX() - other.getX(), dy = getY() - other.getY();
        int skill = 10 + dexterity + level;
        
        // Am I in range?
        if(range > 0) {
            skill += range;
            skill -= (int)Math.sqrt(dx*dx + dy*dy);
            if(skill <= 0) return false;
        } else {
            if(dx < -1 || dx > 1 || dy < -1 || dy > 1) return false;
            // Strength only matters in melee attacks
            skill += strength;
        }
        double hit = Math.random();
        int crit = 0;
        int ac = other.dexterity + other.armor;
        
        // Calculate automatic hit or miss
        if(hit <= .05) {
            new Script(0, 0, "Effect", "miss:Miss!").move(other);
            return true;
        }
        // An automatic hit gives me a chance of a critical hit
        if(hit > .95) crit++;
        
        if(Math.random() * skill >= ac) crit++;
        
        if(crit == 0) {
            new Script(0, 0, "Effect", "blocked:Blocked!").move(other);
            return true;
        }
        // Attack landed - calculate damage
        int damage = 1 + (int)((weapon + strength) * crit * Math.random());
        new Script(0, 0, "Effect", "physical:-" + damage).move(other);
        other.hits -= damage;
        
        if(other.hits <= 0) {
            other.die();
            killed(other);
        }
        return true;
    }
    
    /**
     * This method is called when you have killed another Creature. This only has an effect if it was the player
     * that did the killing; if so, the player gains gold and experience and may level up.
     * @param other Creature that you killed
     */
    public void killed(Creature other) {
        if(!isPlayer()) return;
        // Only the player gains experience and gold from killing monsters. For others, the "experience" and "gold"
        // parameters are there merely to say what the player will gain if he kills them.
        gold += Math.random() * other.gold + Math.random() * other.gold;
        setExperience(experience + other.experience * other.level / level);
    }
    
    /**
     * This method is called when your hit points have gone below zero. 
     */
    public void die() {
        new Script(0, 0, "Field", "" + getTile().getBecome().getNumber()).move(this);
        if(getContainer() != null) {
            ArrayList items = getContainer().getItems();
            for(int i = 0; i < items.size(); i++) {
                Item item = (Item)items.get(i);
                if(item.getCount() == -1) item.setCount(1);
                item.move(this);
            }
        }
        remove();
    }
    
    /**
     * Return this creature's stats in a readable form.
     */
    public String getDescription() {
        String description = "HP: " + hits + "/" + maxHits + "\n";
        description += "MP: " + magic + "/" + maxMagic + "\n";
        description += "Strength: " + strength + "\n";
        description += "Dexterity: " + dexterity + "\n";
        description += "Intelligence: " + intelligence + "\n";
        description += "Armor: " + armor + "\n";
        description += "Weapon: " + weapon;
        return description;
    }
    
    /**
     * Return this creature's vital in a readable form.
     */
    public String getShortDescription() {
        String description = getName() + " (level " + level + ")\n";
        description += "HP: " + hits + "/" + maxHits + "\n";
        description += "MP: " + magic + "/" + maxMagic + "\n";
        if(isPlayer()) {
            description += "Experience: " + (experience / 10) + "/" + (int)(10000 * (Math.pow(1.1, level) - 1) / 10) + "\n";
            description += "Gold: " + gold;
        }
        return description;
    }
    
    public int getGold() {
        return gold;
    }
    
    public void setGold(int gold) {
        if(this.gold != -1) this.gold = gold;
    }
    
    public int getExperience() {
        return experience;
    }
    
    public void setExperience(int experience) {
        this.experience = experience;
        if(experience >= 10000 * (Math.pow(1.1, level) - 1)) {
            level++;
            maxHits = (int)(30 + level * (6 + strength * .5));
            maxMagic = (int)(.5 * intelligence * (3 + level));
            hits += (int)(strength * .5);
            magic += (int)(intelligence * .5);
            
			getLevel().addScript(new Script(getX(), getY(), "Menu", "LevelUp"));
        }
    }
    
    /**
     * Return a comparison of the stats of two creatures
     */
    public String compareToCreature(Creature other) {
        if(other == null) return getDescription();
        
        String description = "";
        if(hits != other.hits || maxHits != other.maxHits) {
            description += "HP: " + other.hits + "/" + other.maxHits + " -> " + hits + "/" + maxHits + "\n";
        } else {
            description += "HP: " + hits + "/" + maxHits + "\n";
        }
        
        if(magic != other.magic || maxMagic != other.maxMagic) {
            description += "MP: " + other.magic + "/" + other.maxMagic + " -> " + magic + "/" + maxMagic + "\n";
        } else {
            description += "MP: " + magic + "/" + maxMagic + "\n";
        }
        
        if(strength != other.strength) {
            description += "Strength: " + other.strength + " -> " + strength + "\n";
        } else {
            description += "Strength: " + strength + "\n";
        }
        
        if(dexterity != other.dexterity) {
            description += "Dexterity: " + other.dexterity + " -> " + dexterity + "\n";
        } else {
            description += "Dexterity: " + dexterity + "\n";
        }
        
        if(intelligence != other.intelligence) {
            description += "Intelligence: " + other.intelligence + " -> " + intelligence + "\n";
        } else {
            description += "Intelligence: " + intelligence + "\n";
        }
        
        if(armor != other.armor) {
            description += "Armor: " + other.armor + " -> " + armor + "\n";
        } else {
            description += "Armor: " + armor + "\n";
        }
        
        if(weapon != other.weapon) {
            description += "Weapon: " + other.weapon + " -> " + weapon;
        } else {
            description += "Weapon: " + weapon;
        }
        return description;
    }
    
    /**
     * Restore this creature to full health.
     */
    public void heal() {
        hits = maxHits;
    }
    
    /**
     * Refill this creatures magic points to the maximum
     */
    public void restoreMagic() {
        magic = maxMagic;
    }
    
    
    public boolean tryMove(int dx, int dy) {
    	return false;
    }

    private void attackPlayer(Creature player) {
    	if(player == null) return;

//  	Try to attack the player
    	if(attack(player)) return;

    	int dx = (player.getX() - getX()), dy = (player.getY() - getY());
    	dx = dx * dx;
    	dy = dy * dy;

//  	Move toward the player if possible
    	if(dx > dy) {
//  		Prefer moving horizontally
    		if(player.getX() <= getX()) {
//  			.1716 is tan squared of 22.5 degrees - can you see why I use it?

    			if(.1716 * dx > dy && tryMove(-1, 0)) return;
    			if(player.getY() <= getY() && (tryMove(-1, -1) || tryMove(-1, 0) || tryMove(0, -1))) return;
    			if(player.getY() >= getY() && (tryMove(-1, 1) || tryMove(-1, 0) || tryMove(0, 1))) return;
    		} else {
    			if(.1716 * dx > dy && tryMove(1, 0)) return;
    			if(player.getY() <= getY() && (tryMove(1, -1) || tryMove(1, 0) || tryMove(0, -1))) return;
    			if(player.getY() >= getY() && (tryMove(1, 1) || tryMove(1, 0) || tryMove(0, 1))) return;
    		}
    	} else {
//  		Prefer moving vertically
    		if(player.getY() <= getY()) {
    			if(.1716 * dy > dx && tryMove(0, -1)) return;
    			if(player.getX() <= getX() && (tryMove(-1, -1) || tryMove(0, -1) || tryMove(-1, 0))) return;
    			if(player.getX() >= getX() && (tryMove(1, -1) || tryMove(0, -1) || tryMove(1, 0))) return;
    		} else {
    			if(.1716 * dy > dx && tryMove(0, 1)) return;
    			if(player.getX() <= getX() && (tryMove(-1, 1) || tryMove(0, 1) || tryMove(-1, 0))) return;
    			if(player.getX() >= getX() && (tryMove(1, 1) || tryMove(0, 1) || tryMove(1, 0))) return;
    		}
    	}
    }
    
    /**
     * This method is called when the player has hit a key to try to move this creature. This method will be your starting
     * point in the fourth part, "Playing". The dx and dy tell you the amount of step in each direction.
     */
    private void keyMove(int dx, int dy) {
        // (mx, my) is the point you are moving to.
        int mx = getX() + dx, my = getY() + dy;
        
        // Fill in the implementation of this method
        
        // Move me to that location
        move(mx, my);
        
        // This will tell the view to redraw itself
        update();
    }
        
    /*
     * The key controls for a creature just call the keyMove() method with different dx and dy values.
     * They also keep track of whether the shift key is down, setting the variable isAction if it is; my
     * suggestion is that if the shift key is held, pressing a direction key just actions that space but
     * does not attempt to move.
     */
    public void keyNumpad7Pressed() { direction = "Northwest"; keyMove(-1, -1); }
    public void keyNumpad8Pressed() { direction = "North"; keyMove( 0, -1); }
    public void keyNumpad9Pressed() { direction = "Northeast"; keyMove( 1, -1); }
    public void keyNumpad6Pressed() { direction = "East"; keyMove( 1,  0); }
    public void keyNumpad3Pressed() { direction = "Southeast"; keyMove( 1,  1); }
    public void keyNumpad2Pressed() { direction = "South"; keyMove( 0,  1); }
    public void keyNumpad1Pressed() { direction = "Southwest"; keyMove(-1,  1); }
    public void keyNumpad4Pressed() { direction = "West"; keyMove(-1,  0); }
    
    public void keyQPressed() { direction = "Northwest"; keyMove(-1, -1); }
    public void keyWPressed() { direction = "North"; keyMove( 0, -1); }
    public void keyEPressed() { direction = "Northeast"; keyMove( 1, -1); }
    public void keyDPressed() { direction = "East"; keyMove( 1,  0); }
    public void keyCPressed() { direction = "Southeast"; keyMove( 1,  1); }
    public void keyXPressed() { direction = "South"; keyMove( 0,  1); }
    public void keyZPressed() { direction = "Southwest"; keyMove(-1,  1); }
    public void keyAPressed() { direction = "West"; keyMove(-1,  0); }
    
    public void keyUpPressed()    { direction = "North"; keyMove( 0, -1); }
    public void keyRightPressed() { direction = "East"; keyMove( 1,  0); }
    public void keyDownPressed()  { direction = "South"; keyMove( 0,  1); }
    public void keyLeftPressed()  { direction = "West"; keyMove(-1,  0); }
    
    public void keyShiftPressed() { isAction = true; }
    public void keyShiftReleased() { isAction = false; }
    
    public void keyMReleased() { getLevel().addScript(new Script(getX(), getY(), "Menu", "Root")); update(); }
}