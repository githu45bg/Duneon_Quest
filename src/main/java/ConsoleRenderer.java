public class ConsoleRenderer implements Renderer {
    private static final int SIGHT_RADIUS = 5;

    @Override
    public void render(Dungeon d) {
        FieldOfView.updateExplored(d, SIGHT_RADIUS);
        StringBuilder sb = new StringBuilder("\n");
        for (int y = 0; y < d.getHeight(); y++) {
            for (int x = 0; x < d.getWidth(); x++) {
                sb.append(cell(d, x, y)).append(' ');
            }
            sb.append('\n');
        }
        System.out.print(sb);
        printStatus(d);
    }

    @Override
    public void showMessage(String message) {
        System.out.println(Ansi.paint(Ansi.YELLOW, message));
    }

    private String cell(Dungeon d, int x, int y) {
        Room room = d.getRoom(x, y);
        boolean visible = FieldOfView.partyCanSee(d, x, y, SIGHT_RADIUS);
        if (!visible && !room.isExplored()) return " ";
        if (!visible) return Ansi.paint(Ansi.DARK, symbolOf(room));
        Character occupant = d.occupantAt(x, y);
        if (occupant != null) return drawCharacter(d, occupant);
        return Ansi.paint(colourFor(room.getType()), symbolOf(room));
    }

    private String symbolOf(Room room) {
        return String.valueOf(room.getType().getSymbol());
    }

    private String drawCharacter(Dungeon d, Character c) {
        if (c instanceof Adventurer a) {
            if (a.isUnconscious()) return Ansi.paint(Ansi.YELLOW, "x");
            int number = d.getAdventurers().indexOf(a) + 1;
            return Ansi.paint(Ansi.CYAN, String.valueOf(number));
        }
        return Ansi.paint(Ansi.RED, c.getName().substring(0, 1));
    }

    private String colourFor(RoomType type) {
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

    private void printStatus(Dungeon d) {
        int number = 1;
        for (Adventurer a : d.getAdventurers()) {
            boolean low = a.getHp() * 3 <= a.getMaxHp();
            String hp = Ansi.paint(low ? Ansi.RED : Ansi.GREEN,
                    a.getHp() + "/" + a.getMaxHp());
            System.out.println("[" + number++ + "] " + a.getName() + " ("
                    + a.getPlayerClass() + ")  HP " + hp + "  MP " + a.getMana());
        }
    }
}
