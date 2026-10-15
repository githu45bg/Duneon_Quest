import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class CaveIn implements Event {
    @Override public String getName() { return "Cave-in"; }

    @Override
    public String trigger(Dungeon d, Scanner in) {
        List<int[]> candidates = corridorCells(d);
        Collections.shuffle(candidates);
        for (int[] c : candidates) {
            d.setRoom(c[0], c[1], RoomType.WALL);
            if (Pathfinder.partyCanReachExit(d)) {
                return "Rocks crash down: the corridor at (" + c[0] + "," + c[1] + ") is blocked!";
            }
            d.setRoom(c[0], c[1], RoomType.FLOOR);
        }
        return "The ceiling groans, but holds.";
    }

    private List<int[]> corridorCells(Dungeon d) {
        List<int[]> cells = new ArrayList<>();
        for (int y = 0; y < d.getHeight(); y++) {
            for (int x = 0; x < d.getWidth(); x++) {
                boolean floor = d.getRoom(x, y).getType() == RoomType.FLOOR;
                if (floor && !d.isOccupied(x, y) && isCorridor(d, x, y)) {
                    cells.add(new int[] {x, y});
                }
            }
        }
        return cells;
    }

    private boolean isCorridor(Dungeon d, int x, int y) {
        boolean vertical = isWall(d, x - 1, y) && isWall(d, x + 1, y);
        boolean horizontal = isWall(d, x, y - 1) && isWall(d, x, y + 1);
        return vertical != horizontal;
    }

    private boolean isWall(Dungeon d, int x, int y) {
        return !d.inBounds(x, y) || d.getRoom(x, y).getType() == RoomType.WALL;
    }
}