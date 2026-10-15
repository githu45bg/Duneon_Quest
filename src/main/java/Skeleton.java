import java.util.List;
import java.util.Optional;

public class Skeleton extends Monster {
    public Skeleton() {
        super("Skeleton", 13, 10, 3, 8, 50);
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
