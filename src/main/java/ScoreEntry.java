import java.util.Optional;

public record ScoreEntry(String name, String playerClass, int level, int treasure, int xp,
                         boolean victory) {

    public int total() { return treasure + xp; }

    public String toLine() {
        return String.join("|", name.replace('|', '_'), playerClass, String.valueOf(level),
                String.valueOf(treasure), String.valueOf(xp), String.valueOf(victory));
    }

    /** Returns empty for blank or damaged lines, so one bad line never breaks the board. */
    public static Optional<ScoreEntry> fromLine(String line) {
        String[] p = line.trim().split("\\|", -1);
        if (p.length != 6) return Optional.empty();
        try {
            return Optional.of(new ScoreEntry(p[0], p[1], Integer.parseInt(p[2]),
                    Integer.parseInt(p[3]), Integer.parseInt(p[4]), Boolean.parseBoolean(p[5])));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}