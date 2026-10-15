public final class Palette {
    private Palette() { }

    public static String colourFor(RoomType type) {
        return switch (type) {
            case WALL     -> Ansi.BRIGHT;
            case FLOOR    -> Ansi.WHITE;
            case TREASURE -> Ansi.YELLOW;
            case POTION   -> Ansi.MAGENTA;
            case KEY      -> Ansi.CYAN;
            case CHEST    -> Ansi.YELLOW;
            case EXIT     -> Ansi.GREEN;
            case TRAP     -> Ansi.RED;
            case SECRET   -> Ansi.YELLOW;
        };
    }
}