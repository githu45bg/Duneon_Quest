import java.util.Comparator;
import java.util.Optional;
// cast actions handles magic spell action
// mage - cast fireball
// non-mage -heal

public class CastAction implements Action {
    private static final int FIREBALL_COST = 3;
    private static final int FIREBALL_RANGE = 5;
    private static final int HEAL_COST = 3;

    private final Adventurer caster;
    //This stores the adventurer who is casting the spell.
    //constructor- create a cast action

    public CastAction(Adventurer caster) { this.caster = caster; }
    // stores magic inside the action

    public static int manaCost(PlayerClass pc) {
        return pc == PlayerClass.MAGE ? FIREBALL_COST : HEAL_COST;
    }
    //determines how much mana the spell costs based on character class

    public static boolean hasTarget(Adventurer caster, Dungeon d) {
        return caster.getPlayerClass() == PlayerClass.MAGE
                ? fireballTarget(caster, d).isPresent()
                : healTarget(caster, d).isPresent();
    }// if mage ; finds a fireball target,rue,
    // otherwise a healing target

    @Override
    public String execute(Dungeon d) {// whether the caster has enough mana
        if (caster.getMana() < manaCost(caster.getPlayerClass())) {
            return caster.getName() + " is out of mana.";
        }
        return caster.getPlayerClass() == PlayerClass.MAGE ? fireball(d) : heal(d);
    }

    private String fireball(Dungeon d) {// choosing fireball or heal
        Optional<Monster> target = fireballTarget(caster, d);
        if (target.isEmpty()) return "The spell fizzles: no target in sight.";
        caster.spendMana(FIREBALL_COST);
        int damage = Dice.roll(2, 6);
        target.get().takeDamage(damage);
        return caster.getName() + " hurls a Fireball at " + target.get().getName()
                + " for " + damage + " damage!";
    }

    private String heal(Dungeon d) {// performs healing
        Optional<Adventurer> target = healTarget(caster, d);
        if (target.isEmpty()) return "Nobody nearby needs healing.";
        caster.spendMana(HEAL_COST);
        Adventurer ally = target.get();
        boolean wasDown = ally.isUnconscious();
        ally.heal(Dice.roll(1, 8) + 2);
        return caster.getName() + (wasDown ? " revives " : " heals ") + ally.getName() + "!";
    }

    private static Optional<Monster> fireballTarget(Adventurer c, Dungeon d) {// finding fireball target
        return d.getMonsters().stream()
                .filter(m -> m.isAlive())
                .filter(m -> FieldOfView.canSee(d, c.getX(), c.getY(),
                        m.getX(), m.getY(), FIREBALL_RANGE))
                .min(Comparator.comparingInt((Monster m) -> distance(c, m)));
    }

    private static Optional<Adventurer> healTarget(Adventurer c, Dungeon d) {
        return d.getAdventurers().stream()
                .filter(a -> !a.isDead() && a.getHp() < a.getMaxHp())// check line of sight and range
                .filter(a -> distance(c, a) <= 1)
                .min(Comparator.comparingInt((Adventurer a) -> a.getHp()));//clossest
    }

    private static int distance(Character a, Character b) {// calculate attack distance
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
}