import java.util.List;
import java.util.Optional;
import java.util.Scanner;
// goblins jump out

public class Ambush implements Event {
    /*POLYMORPHISM, @OVERIIDE= this places abstract method in the event.
    DM console looks every event by this name, so event ambush finds this class
     */
    @Override public String getName() { return "Ambush"; }
    /*polymorphism event.trigger(...) works on ambush and
     other classes and each does something different
     */

    @Override
    public String trigger(Dungeon d, Scanner in) {
        List<Adventurer> alive = d.livingAdventurers();
        /*Encapsulation Dungeon private list of adventures is not touched
        livingAdventurers() hands back a filtered read only copy
        so nothing here changes the dungeons own data
         */
        if (alive.isEmpty()) return "Nobody is left to ambush.";
        // Goblins really placed (there may not be room for all of them).

        int count = Dice.roll(1, 4);
        int placed = 0;
        for (int i = 0; i < count; i++) {
            Adventurer victim = Dice.pick(alive);
            Optional<int[]> spot = Pathfinder.nearestFree(d, victim.getX(), victim.getY());
            if (spot.isEmpty()) break;
            Goblin goblin = new Goblin();
            goblin.moveTo(spot.get()[0], spot.get()[1]);
            d.addMonster(goblin);
            placed++;
        }
        // [MODULARISATION] This class RETURNS a message and never prints it.
        // Showing it on screen is the renderer's job: logic and display are separate.

        return placed + " goblin(s) leap out of the shadows!";
    }
}