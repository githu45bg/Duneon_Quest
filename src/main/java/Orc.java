import java.util.List;
import java.util.Optional;

public class Orc extends Monster {
    public Orc() {
        super("Orc", 20, 13, 4, 10, 75);
    }

    private boolean enraged() {
        return getHp() * 2 <= getMaxHp();
    }

    @Override
    public int rollDamage() {
        return super.rollDamage() + (enraged() ? 3 : 0);
    }

    @Override
    public Action chooseAction(Dungeon dungeon, List<Adventurer> targets) {
        Optional<Adventurer> target = nearest(targets);
        if (target.isEmpty()) return new RestAction(this);

        Adventurer foe = target.get();
        if (distanceTo(foe) == 1) return new AttackAction(this, foe);
        return stepToward(dungeon, foe);
    }
}