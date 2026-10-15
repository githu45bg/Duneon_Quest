public class Adventurer extends Character {
    // the adventurer class represents a playable character in the game
    // Extends the character class adds player specific functionality

    private static final int REVIVE_WINDOW = 3;/*Number of rounds an unconcious player has
    before becoming permanently dead*/

    private final PlayerClass playerClass;
    private final Inventory inventory;
    private int mana;
    private Weapon weapon;
    private int armorBonus;
    private int reviveRoundsLeft;
    private boolean dead;
    private int gold;
    private int xp;
    private int abilityCooldown;
    private Armor armor;
    private static final int MAX_LEVEL = 10;
    private static final int XP_STEP = 50;
    private static final int HP_PER_LEVEL = 5;
    private static final int MANA_PER_LEVEL = 3;

    private int level = 1;

    // Default constructor-creates an adventurer at coordinates(0,0)
    public Adventurer(String name, PlayerClass playerClass) {
        this(name, playerClass, 0, 0);
    }

    // Custom constructor-initializes the adventurer using the statistics associated with selected playerClass
    public Adventurer(String name, PlayerClass playerClass, int x, int y) {
        super(//calls the constructor of the parent charactrer class
                name,
                playerClass.getMaxHP(),
                playerClass.getArmorClass(),
                x,
                y
        );

        this.playerClass = playerClass;// store the selected player class
        this.mana = playerClass.getMaxMana();// start the player with their maximum mana
        this.inventory = new Inventory();//creates an empty inventory
        this.reviveRoundsLeft = REVIVE_WINDOW;// set individual revival period
        this.armorBonus = 0;//no additional armor at the beginning
        this.dead = false;// player starts alive
        this.armor=armor;/* this line assigns the field to itself
        since the constructor has no armor, armor will remain null*/
    }

    private final StatusEffects effects = new StatusEffects();
    public StatusEffects getEffects() { return effects; }

    //------------------ABILITY COOLDOWN--------------------------
    //      * Checks whether the player's special ability is ready to use.

    public boolean abilityReady()              { return abilityCooldown == 0; }
    public int getAbilityCooldown()            { return abilityCooldown; }
    public void startAbilityCooldown(int rounds) { abilityCooldown = rounds; }

    //-----------------COMBAT-------------------------------------
        // * Calculates the player's attack modifier.


    @Override
    public int getAttackModifier() {
        return Math.max(playerClass.getStrMod(), playerClass.getDexMod()) + effects.rollPenalty();
    }

    @Override
    protected int getDexModifier() { return playerClass.getDexMod() + effects.rollPenalty(); }

    @Override
    public int rollDamage() {
        int die = (weapon == null) ? playerClass.getWeaponDie() : weapon.getDamageDie();
        return Math.max(0, Dice.roll(1, die) + playerClass.getStrMod() + effects.rollPenalty());
    }

    // ---------------- HEALTH ----------------

    @Override
    public void heal(int amount) {
        if (!dead && amount > 0) {
            super.heal(amount);
        }
    }



    public boolean isUnconscious() {
        return getHp() == 0 && !dead;
    }

    public boolean isDead() {
        return dead;
    }

    public Armor getArmor() { return armor; }

    public void restoreState(int hp, int mana, int reviveRoundsLeft, boolean dead) {
        takeDamage(getMaxHp() - hp);
        this.mana = Math.max(0, Math.min(mana, maxMana()));
        this.reviveRoundsLeft = reviveRoundsLeft;
        this.dead = dead;
    }
    // Called at the end of each round
    public void endOfRound() {
        effects.tick();
        if (abilityCooldown > 0) abilityCooldown--;

        if (!isUnconscious()) {
            reviveRoundsLeft = REVIVE_WINDOW;
            return;
        }
        reviveRoundsLeft--;
        if (reviveRoundsLeft <= 0) {
            dead = true;
        }
    }
    public boolean spendGold(int amount) {
        if (amount < 0 || amount > gold) return false;
        gold -= amount;
        return true;
    }

    // ---------------- MANA ----------------

    public boolean spendMana(int cost) {

        if (cost < 0 || cost > mana) {
            return false;
        }

        mana -= cost;
        return true;
    }
    public void addGold(int amount){gold +=Math.max(0, amount);}
    /** XP needed in total to reach a level: 100 for level 2, 300 for level 3, 600 for level 4... */
    public static int xpForLevel(int level) { return XP_STEP * level * (level - 1); }

    /** Adds XP and returns how many levels were gained. */
    public int addXp(int amount) {
        xp += Math.max(0, amount);
        int gained = 0;
        while (level < MAX_LEVEL && xp >= xpForLevel(level + 1)) {
            level++;
            gained++;
            raiseMaxHp(HP_PER_LEVEL);
            mana += manaPerLevel();
        }
        return gained;
    }

    /** For loading: sets XP and the level it earns, at full HP and mana, with no announcement. */
    public void restoreXp(int totalXp) {
        xp = Math.max(0, totalXp);
        while (level < MAX_LEVEL && xp >= xpForLevel(level + 1)) {
            level++;
            raiseMaxHp(HP_PER_LEVEL);
        }
        mana = maxMana();
    }

    public int getLevel() { return level; }
    private int manaPerLevel() { return playerClass.canCast() ? MANA_PER_LEVEL : 0; }
    private int maxMana() {
        return playerClass.canCast()
                ? playerClass.getMaxMana() + (level - 1) * MANA_PER_LEVEL : 0;
    }
    public int getGold(){return gold;}
    public int getXp(){return xp;}

    public void restoreMana(int amount) {

        if (amount <= 0) {
            return;
        }

        mana = Math.min(
                maxMana(),
                mana + amount
        );
    }

    // ---------------- EQUIPMENT ----------------

    public void equipWeapon(Weapon weapon) {
        this.weapon = weapon;
    }

    public void equipArmor(Armor armor) {

        if (armor == null) {
            return;
        }

        setArmorClass(
                super.getArmorClass()
                        - armorBonus
                        + armor.getAcBonus()
        );

        armorBonus = armor.getAcBonus();
    }
    @Override
    public int getArmorClass() { return super.getArmorClass() + effects.acBonus(); }

    // ---------------- GETTERS ----------------

    public PlayerClass getPlayerClass() {
        return playerClass;
    }
    public Inventory getInventory() {
        return inventory;
    }
    public int getMana() {
        return mana;
    }
    public Weapon getWeapon() {
        return weapon;
    }
    public int getArmorBonus() {
        return armorBonus;
    }
    public int getReviveRoundsLeft() {
        return reviveRoundsLeft;
    }
}