import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/** Saves and loads whole games as plain text in the saves folder. */
public final class GameSaver {
    private static final Path DIR = Path.of("saves");

    private GameSaver() { }

    public static void save(String name, int levelIndex, Dungeon d, DungeonMaster dm)
            throws SaveException {
        Path file = pathFor(name);
        try {
            Files.createDirectories(DIR);
            Files.write(file, SaveWriter.build(levelIndex, d, dm));
        } catch (IOException e) {
            throw new SaveException("Could not write " + file + ".");
        }
    }

    public static SavedGame load(String name) throws SaveException {
        Path file = pathFor(name);
        try {
            return SaveReader.parse(Files.readAllLines(file));
        } catch (IOException e) {
            throw new SaveException("Could not read save '" + name.trim() + "'. Does it exist?");
        }
    }

    public static List<String> listSaves() {
        try (Stream<Path> files = Files.list(DIR)) {
            return files.map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(".txt"))
                    .map(n -> n.substring(0, n.length() - 4))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    /** Only simple names are allowed, so nobody can write outside the saves folder. */
    private static Path pathFor(String name) throws SaveException {
        String clean = name.trim();
        if (!clean.matches("[A-Za-z0-9_-]{1,30}")) {
            throw new SaveException("Save names can use letters, digits, - and _ (up to 30).");
        }
        return DIR.resolve(clean + ".txt");
    }
}