public class Potion extends Item {
    private final int healAmount;

    public Potion(String name, int healAmount) {
        super(name, 10);
        this.healAmount = healAmount;
    }
    public int getHealAmount() { return healAmount; }     // Potion
    // Scroll

    @Override
    public String use(Adventurer user) {
        if (user.isDead()) return "It is too late for that.";
        user.heal(healAmount);
        return user.getName() + " drinks " + getName() + " and recovers "
                + healAmount + " HP.";
    }
}
