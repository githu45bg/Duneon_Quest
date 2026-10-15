public abstract class Character {// abstract = provides common funtionality while leaving some details for child
    // parent class. contain behaviors of character in the game
    private final String name;
    private int maxHP;
    private int hp;
    private int armorClass;
    private int x;
    private int y;
    // fields of a character

    protected Character(String name, int maxHP, int armorClass,
                        int x, int y){
        this.name=name;
        this.maxHP=maxHP;
        this.hp=maxHP;
        this.armorClass=armorClass;
        // represent character position on the dungeon grid
        this.x=x;
        this.y=y;

    }
    // the constructor can  be accessed by the class itself
    protected Character(String name, int maxHp, int armorClass){
        this(name, maxHp, armorClass, 0,0);
    }
    public abstract int getAttackModifier();
    public abstract int rollDamage();

    public int rollInitiative(){
        return Dice.rollWithModifier(1,20,getDexModifier());
    }
    protected abstract int getDexModifier();
    public boolean attackRoll(Character target){
        int roll=Dice.rollWithModifier(1,20,getAttackModifier());
        return roll>=target.getArmorClass();
    }
    // calculate s the damage taken on character
    public void takeDamage(int amount){
        hp=Math.max(0,hp-Math.max(0,amount));
    }
    public void heal(int amount){
        hp=Math.min(maxHP,hp+Math.max(0,amount));
    }
    public void moveTo(int newX, int newY) { x = newX; y = newY; }
    public boolean isAlive(){return hp>0;}
    public void moveTO(int newX, int newY){x=newX; y=newY;}

    public String getName(){return name;}
    public int getHp()         { return hp; }
    public int getMaxHp()      { return maxHP; }
    public int getArmorClass() { return armorClass; }
    public int getX()          { return x; }
    public int getY()          { return y; }
    protected void setArmorClass(int ac) { this.armorClass = ac; }

    protected void raiseMaxHp(int amount) {
        maxHP += amount;
        hp += amount;
    }

}
