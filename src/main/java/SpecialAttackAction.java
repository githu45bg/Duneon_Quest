public class SpecialAttackAction implements Action {
    private static final int COOLDOWN = 3;

    private final Adventurer attacker;
    private final Monster target;

    public SpecialAttackAction(Adventurer attacker, Monster target) {
        this.attacker = attacker;
        this.target = target;
    }

    @Override
    public String execute(Dungeon d) {
        attacker.startAbilityCooldown(COOLDOWN);
        boolean warrior = attacker.getPlayerClass() == PlayerClass.WARRIOR;
        String move = attacker.getPlayerClass().getAbilityName();

        int toHit = attacker.getAttackModifier() + (warrior ? -2 : 4);
        if (Dice.rollWithModifier(1, 20, toHit) < target.getArmorClass()) {
            return attacker.getName() + " tries " + move + " but misses!";
        }
        int damage = warrior ? attacker.rollDamage() * 2
                : attacker.rollDamage() + Dice.roll(1, 6);
        target.takeDamage(damage);
        return attacker.getName() + " lands " + move + " on " + target.getName()
                + " for " + damage + " damage!";
    }
}