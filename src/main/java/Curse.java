import java.util.List;
import java.util.Scanner;

public class Curse implements Event {
    private static final int DURATION = 3;

    @Override public String getName() { return "Curse"; }

    @Override
    public String trigger(Dungeon d, Scanner in) {
        List<Adventurer> alive = d.livingAdventurers();
        if (alive.isEmpty()) return "Nobody is left to curse.";
        Adventurer victim = Dice.pick(alive);
        victim.getEffects().curse(DURATION);
        return "A dark shadow falls on " + victim.getName()
                + ": -1 to all rolls for " + DURATION + " rounds!";
    }
}
