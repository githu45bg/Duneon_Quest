import java.util.Optional;

public enum Direction {//finds the direction
    N(0, -1), S(0, 1), E(1, 0), W(-1, 0);
    //locates the positiom

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) { this.dx = dx; this.dy = dy; }
    // retuns the position

    public int dx() { return dx; }
    public int dy() { return dy; }

    /** Safe parsing of player input: never throws. */
    public static Optional<Direction> parse(String text) {
        if (text == null) return Optional.empty();
        for (Direction d : values()) {
            if (d.name().equalsIgnoreCase(text.trim())) return Optional.of(d);
        }
        return Optional.empty();
    }
}
