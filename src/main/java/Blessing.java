import java.util.List;
import java.util.Scanner;
/*Random class event
it randomly chooses living adventurer and temporary gives +2AC FOR 3 ROUNDS
 */
public class Blessing implements Event {
    private static final int DURATION = 3; //3 ROUNDS

    @Override public String getName() { return "Blessing"; }
    //get name()
    //returns the name of the event

    @Override
    public String trigger(Dungeon d, Scanner in) {
        // Executing the blessing event
        List<Adventurer> alive = d.livingAdventurers();
        //finds list of adventurers who are still alive
        if (alive.isEmpty()) return "Nobody is left to bless.";//whether anyone is alive
        //picks a random adventurer
        Adventurer lucky = Dice.pick(alive);
        // apply a blessing
        lucky.getEffects().bless(DURATION);
        return "A warm light surrounds " + lucky.getName()
                + ": +2 AC for " + DURATION + " rounds!";// message
    }
}
