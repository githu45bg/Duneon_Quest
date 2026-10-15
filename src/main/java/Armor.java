public class Armor extends Item {
    // This means Armor inherits properties and methods from the Item class.
    // 'final' means the value cannot be changed after it is assigned.


    private final int acBonus;
    //constructor for creating armor objects

    public Armor(String name, int acBonus, int value) {
        // Calls the constructor of the parent class (Item).
        // It passes the armor's name and value to Item.
        super(name, value);
        // Stores the AC bonus provided by this armor.

        this.acBonus = acBonus;
    }

    public int getAcBonus() { return acBonus; }
    // Overrides the use() method inherited from the Item class.
    // This method is called when an Adventurer uses/equips the armor.

    @Override
    public String use(Adventurer user) {
        // Tells the Adventurer object to equip this armor.
        // 'this' refers to the current Armor object.
        user.equipArmor(this);
        // Returns a message describing what happened.

        return user.getName() + " puts on " + getName() + " (+" + acBonus + " AC).";
    }
}
