import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public abstract class Monster extends Character {
    private final int attackMod;
    private final int damageDie;
    private final int xpValue;

    protected Monster(String name, int maxHp, int ac,
                      int attackMod, int damageDie, int xpValue) {
        super(name, maxHp, ac);
        this.attackMod = attackMod;
        this.damageDie = damageDie;
        this.xpValue = xpValue;
    }

    /** Each monster type decides its own behaviour. */
    public abstract Action chooseAction(Dungeon dungeon, List<Adventurer> targets);

    @Override public int getAttackModifier() { return attackMod; }
    @Override protected int getDexModifier() { return attackMod; }
    @Override public int rollDamage()        { return Dice.roll(1, damageDie); }
    public int getXpValue()                  { return xpValue; }

    // ---- shared AI helpers ----

    protected Optional<Adventurer> nearest(List<Adventurer> targets) {
        return targets.stream()
                .filter(a -> !a.isDead() && !a.isUnconscious())
                .min(Comparator.comparingInt(a -> distanceTo(a)));
    }

    protected int distanceTo(Character other) {
        return Math.abs(getX() - other.getX()) + Math.abs(getY() - other.getY());
    }

    protected Action stepToward(Dungeon dungeon, Character target) {
        return bestStep(dungeon, target, true);
    }

    protected Action stepAwayFrom(Dungeon dungeon, Character target) {
        return bestStep(dungeon, target, false);
    }

    private Action bestStep(Dungeon dungeon, Character target, boolean closer) {
        Direction best = null;
        int bestDist = distanceTo(target);
        for (Direction d : Direction.values()) {
            int nx = getX() + d.dx();
            int ny = getY() + d.dy();
            if (!dungeon.canEnter(nx, ny)) continue;
            int dist = Math.abs(nx - target.getX()) + Math.abs(ny - target.getY());
            if (closer ? dist < bestDist : dist > bestDist) {
                best = d;
                bestDist = dist;
            }
        }
        return best == null ? new RestAction(this) : new MoveAction(this, best);
    }
}