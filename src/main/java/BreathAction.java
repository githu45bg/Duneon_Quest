import java.util.ArrayList;
import java.util.List;
import java.util.Optional;// method might not have a result
// dragon fire breathing attack in level 3
// this attack travels through a straight line and can damage multiple characters
public class BreathAction implements Action {// class declaration
    // fields stored
    private final Monster dragon;
    private final Direction direction;
    private final int range;

    //constructor =creates the action

    public BreathAction(Monster dragon, Direction direction, int range) {
        this.dragon = dragon;
        this.direction = direction;
        this.range = range;
        // dragon breathes in a certain direction with a certain range
    }

    /**
     * The direction that would burn the most adventurers, if any.
     */
    public static Optional<Direction> findLine(Monster m, Dungeon d, int range) {
        Direction best = null;
        // best = stores the direction with most targets
        // stores the largest number of adventurers found so far
        int most = 0;
        for (Direction dir : Direction.values()) {
            //check every direction
            int count = inLine(m, dir, d, range).size();
            //find all adventurers that the dragon could hit in that direction.
            if (count > most) {
                most = count;
                best = dir;
            }
        }
        //is possible if no adventurer is found.
        return Optional.ofNullable(best);
    }

    private static List<Adventurer> inLine(Character src, Direction dir, Dungeon d, int range) {
        //Which adventurers are directly in front of the dragon?
        List<Adventurer> hit = new ArrayList<>();
        // create a hit list
        //loop through the attack range, by checking each grid position
        for (int step = 1; step <= range; step++) {
            // this calculates where fire is at the current steps
            int x = src.getX() + dir.dx() * step;/*scr.get()gives the starting X coordinate.(calculate coordinates)
            dir.dx() gives the X movement for the direction*/
            int y = src.getY() + dir.dy() * step;// this is for y coordinate
            if (!d.inBounds(x, y) || d.getRoom(x, y).blocksSight()) break;/*check dungeon boundaries
            fire stop when there is a wall*/
            Character c = d.occupantAt(x, y);
            if (c instanceof Adventurer a && !a.isUnconscious()) hit.add(a);/*
            checks whether is an adventurer, also checks whether is concious*/
        }
        return hit;
    }

    @Override
    public String execute(Dungeon d) {//actual attack. fire breath
        StringBuilder sb = new StringBuilder(
                dragon.getName() + " breathes fire to the " + direction + "!");
        for (Adventurer a : inLine(dragon, direction, d, range)) {//find all targets
            int damage = Dice.roll(2, 6);// calculate damage
            a.takeDamage(damage);// appy damage
            sb.append("\n  ").append(a.getName())// add the damage message
                    .append(" is scorched for ").append(damage).append(" damage!");
        }
        return sb.toString();
    }
}
