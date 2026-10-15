import java.util.List;
import java.util.Optional;

public class Goblin extends Monster {
    public Goblin() {
        super("Goblin", 7, 12, 2, 6, 25);
    }

    @Override
    public Action chooseAction(Dungeon dungeon, List<Adventurer> targets) {
        Optional<Adventurer> target = nearest(targets);
        if (target.isEmpty()) return new RestAction(this);

        Adventurer foe = target.get();
        boolean wounded = getHp() * 4 <= getMaxHp();   // 25% HP or less
        if (wounded) return stepAwayFrom(dungeon, foe);
        if (distanceTo(foe) == 1) return new AttackAction(this, foe);
        return stepToward(dungeon, foe);
    }
}