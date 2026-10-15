import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class LevelLoader {
    private LevelLoader() { }

    public static Dungeon load(Path file) throws LevelLoadException {
        List<String> lines = readLines(file);
        int width = lines.get(0).length();
        for (String line : lines) {
            if (line.length() != width) {
                throw new LevelLoadException("Level rows must all be the same length.");
            }
        }
        Dungeon dungeon = new Dungeon(width, lines.size());
        boolean hasStart = false;
        boolean hasExit = false;
        for (int y = 0; y < lines.size(); y++) {
            for (int x = 0; x < width; x++) {
                char c = lines.get(y).charAt(x);
                hasStart |= placeSymbol(dungeon, c, x, y);
                hasExit  |= (c == 'E');
            }
        }
        if (!hasStart) throw new LevelLoadException("Level has no start (A).");
        if (!hasExit)  throw new LevelLoadException("Level has no exit (E).");
        return dungeon;
    }

    private static List<String> readLines(Path file) throws LevelLoadException {
        try {
            List<String> lines = Files.readAllLines(file);
            lines.removeIf(String::isBlank);
            if (lines.isEmpty()) throw new LevelLoadException("Level file is empty.");
            return lines;
        } catch (IOException e) {
            throw new LevelLoadException("Could not read level file: " + file);
        }
    }

    /** Returns true if the symbol was the player start. */
    private static boolean placeSymbol(Dungeon d, char c, int x, int y)
            throws LevelLoadException {
        switch (c) {
            case 'A' -> { d.setRoom(x, y, RoomType.FLOOR); d.setStart(x, y); return true; }
            case 'G' -> spawn(d, new Goblin(), x, y);
            case 'S' -> spawn(d, new Skeleton(), x, y);
            case 'O' -> spawn(d, new Orc(), x, y);
            case 'D' -> spawn(d, new Dragon(), x, y);// your class
            default -> {
                RoomType type = RoomType.fromSymbol(c);
                if (type == null) {
                    throw new LevelLoadException("Unknown symbol '" + c + "' at " + x + "," + y);
                }
                d.setRoom(x, y, type);
            }
        }
        return false;
    }

    private static void spawn(Dungeon d, Monster m, int x, int y) {
        d.setRoom(x, y, RoomType.FLOOR);
        m.moveTo(x, y);
        d.addMonster(m);
    }
}