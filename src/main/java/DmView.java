public final class DmView {
    private DmView() { }

    public static void show(Dungeon d) {
        StringBuilder sb = new StringBuilder("\n");
        sb.append(Ansi.paint(Ansi.MAGENTA, "===== DUNGEON MASTER VIEW (x across, y down) ====="));
        sb.append("\n   ");
        for (int x = 0; x < d.getWidth(); x++) sb.append(String.format("%-3d", x));
        sb.append('\n');
        for (int y = 0; y < d.getHeight(); y++) {
            sb.append(String.format("%2d ", y));
            for (int x = 0; x < d.getWidth(); x++) sb.append(cell(d, x, y)).append("  ");
            sb.append('\n');
        }
        System.out.print(sb);
        listDetails(d);
    }

    private static String cell(Dungeon d, int x, int y) {
        Character c = d.occupantAt(x, y);
        if (c instanceof Adventurer a) {
            return Ansi.paint(Ansi.CYAN, String.valueOf(d.getAdventurers().indexOf(a) + 1));
        }
        if (c != null) return Ansi.paint(Ansi.RED, c.getName().substring(0, 1));
        RoomType type = d.getRoom(x, y).getType();
        return Ansi.paint(Palette.colourFor(type), String.valueOf(type.getSymbol()));
    }

    private static void listDetails(Dungeon d) {
        d.find(RoomType.EXIT).ifPresent(e ->
                System.out.println("Exit at (" + e[0] + "," + e[1] + ")"));
        System.out.println("Monsters (true HP):");
        for (Monster m : d.getMonsters()) {
            System.out.println("  " + m.getName() + " at (" + m.getX() + "," + m.getY()
                    + ")  HP " + m.getHp() + "/" + m.getMaxHp());
        }
    }
}