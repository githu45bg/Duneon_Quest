import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Appends results to scores.txt and shows the best ones. */
public class ScoreBoard {
    private final Path file;

    public ScoreBoard(Path file) { this.file = file; }

    /** One line per adventurer. Returns false if the file could not be written. */
    public boolean append(List<Adventurer> party, int level, boolean victory) {
        List<String> lines = party.stream()
                .map(a -> entryFor(a, level, victory).toLine())
                .toList();
        try {
            Files.write(file, lines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private ScoreEntry entryFor(Adventurer a, int level, boolean victory) {
        int treasure = a.getGold() + a.getInventory().totalValue();
        return new ScoreEntry(a.getName(), a.getPlayerClass().name(), level, treasure,
                a.getXp(), victory && !a.isDead());
    }

    public List<ScoreEntry> readAll() {
        try {
            return Files.readAllLines(file).stream()
                    .map(ScoreEntry::fromLine)
                    .flatMap(Optional::stream)
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    public String topScores(int count) {
        List<ScoreEntry> top = readAll().stream()
                .sorted(Comparator.comparingInt(ScoreEntry::total).reversed())
                .limit(count)
                .toList();
        if (top.isEmpty()) return "No scores yet.";
        StringBuilder sb = new StringBuilder("----- TOP SCORES -----\n");
        sb.append(String.format("%-3s %-12s %-8s %-4s %-9s %-6s %s%n",
                "#", "Name", "Class", "Lvl", "Treasure", "XP", "Result"));
        int rank = 1;
        for (ScoreEntry e : top) {
            sb.append(String.format("%-3d %-12s %-8s %-4d %-9d %-6d %s%n", rank++, e.name(),
                    e.playerClass(), e.level(), e.treasure(), e.xp(), e.victory() ? "WON" : "fell"));
        }
        return sb.toString();
    }
}
