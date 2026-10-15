import java.util.List;
import java.util.Optional;

public class Dragon extends Monster {
    private static final int BREATH_RANGE = 6;
    private static final int BREATH_COOLDOWN = 3;

    private int breathCooldown;

    public Dragon() {
        super("Dragon", 40, 15, 5, 10, 200);
    }

    @Override
    public Action chooseAction(Dungeon dungeon, List<Adventurer> targets) {
        if (breathCooldown > 0) breathCooldown--;
        Optional<Adventurer> target = nearest(targets);
        if (target.isEmpty()) return new RestAction(this);

        if (breathCooldown == 0) {
            Optional<Direction> line = BreathAction.findLine(this, dungeon, BREATH_RANGE);
            if (line.isPresent()) {
                breathCooldown = BREATH_COOLDOWN;
                return new BreathAction(this, line.get(), BREATH_RANGE);
            }
        }
        Adventurer foe = target.get();
        if (distanceTo(foe) == 1) return new AttackAction(this, foe);
        return stepToward(dungeon, foe);
    }
}
