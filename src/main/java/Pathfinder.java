import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

public final class Pathfinder {
    private Pathfinder() { }

    /** Every walkable cell connected to (sx, sy), nearest first. */
    public static List<int[]> reachable(Dungeon d, int sx, int sy) {
        List<int[]> order = new ArrayList<>();
        boolean[][] seen = new boolean[d.getHeight()][d.getWidth()];
        Deque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[] {sx, sy});
        seen[sy][sx] = true;
        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            order.add(cur);
            for (Direction dir : Direction.values()) {
                int nx = cur[0] + dir.dx();
                int ny = cur[1] + dir.dy();
                if (d.inBounds(nx, ny) && !seen[ny][nx] && d.getRoom(nx, ny).isWalkable()) {
                    seen[ny][nx] = true;
                    queue.add(new int[] {nx, ny});
                }
            }
        }
        return order;
    }

    public static Optional<int[]> nearestFree(Dungeon d, int sx, int sy) {
        for (int[] cell : reachable(d, sx, sy)) {
            if (d.canEnter(cell[0], cell[1])) return Optional.of(cell);
        }
        return Optional.empty();
    }

    public static boolean partyCanReachExit(Dungeon d) {
        Optional<int[]> exit = d.find(RoomType.EXIT);
        if (exit.isEmpty()) return false;
        List<int[]> reach = reachable(d, exit.get()[0], exit.get()[1]);
        for (Adventurer a : d.livingAdventurers()) {
            if (reach.stream().noneMatch(c -> c[0] == a.getX() && c[1] == a.getY())) {
                return false;
            }
        }
        return true;
    }
}
