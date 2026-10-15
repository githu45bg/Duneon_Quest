public final class FieldOfView {

    private FieldOfView() { }

    public static final int SIGHT_RADIUS = 5;

    public static boolean canSee(
            Dungeon d,
            int x0, int y0,
            int x1, int y1,
            int radius) {

        int dx = x1 - x0;
        int dy = y1 - y0;

        if (dx * dx + dy * dy > radius * radius) {
            return false;
        }

        return lineClear(d, x0, y0, x1, y1);
    }

    /**
     * Walls block sight, but the wall itself remains visible.
     */
    private static boolean lineClear(
            Dungeon d,
            int x0, int y0,
            int x1, int y1) {

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx - dy;

        int x = x0;
        int y = y0;

        while (x != x1 || y != y1) {

            int e2 = 2 * err;

            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }

            if (e2 < dx) {
                err += dx;
                y += sy;
            }

            boolean atTarget = (x == x1 && y == y1);

            if (!atTarget && d.getRoom(x, y).blocksSight()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if any living adventurer can see a tile.
     */
    public static boolean partyCanSee(
            Dungeon d, int x, int y, int radius) {

        for (Adventurer a : d.getAdventurers()) {

            if (!a.isDead()
                    && canSee(
                    d,
                    a.getX(), a.getY(),
                    x, y,
                    radius)) {

                return true;
            }
        }

        return false;
    }

    /**
     * Marks currently visible rooms as explored.
     * Previously explored rooms remain explored.
     */
    public static void updateExplored(Dungeon d, int radius) {

        for (int y = 0; y < d.getHeight(); y++) {

            for (int x = 0; x < d.getWidth(); x++) {

                if (partyCanSee(d, x, y, radius)) {
                    d.getRoom(x, y).markExplored();
                }
            }
        }
    }
}