import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Builds the lines of a save file. Format: header, then [SECTIONS]. */
final class SaveWriter {
    private SaveWriter() { }

    static List<String> build(int levelIndex, Dungeon d, DungeonMaster dm) {
        List<String> out = new ArrayList<>();
        out.add("DUNGEONQUEST-SAVE 1");
        out.add("level=" + levelIndex);
        out.add("size=" + d.getWidth() + "," + d.getHeight());
        out.add("start=" + d.getStartX() + "," + d.getStartY());
        out.add("dm=" + dm.getTrapsLeft() + "," + dm.getSpawnsLeft() + "," + dm.getEventsLeft());
        addGrid(out, "GRID", d, r -> String.valueOf(r.getType().getSymbol()));
        addGrid(out, "EXPLORED", d, r -> r.isExplored() ? "1" : "0");
        addGrid(out, "REVEALED", d, r -> r.isRevealed() ? "1" : "0");
        addMonsters(out, d);
        addParty(out, d);
        return out;
    }

    private static void addGrid(List<String> out, String title, Dungeon d, Function<Room, String> cell) {
        out.add("[" + title + "]");
        for (int y = 0; y < d.getHeight(); y++) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < d.getWidth(); x++) row.append(cell.apply(d.getRoom(x, y)));
            out.add(row.toString());
        }
    }

    private static void addMonsters(List<String> out, Dungeon d) {
        out.add("[MONSTERS]");
        for (Monster m : d.getMonsters()) {
            if (m.isAlive()) out.add(row(m.getName(), m.getX(), m.getY(), m.getHp()));
        }
    }

    private static void addParty(List<String> out, Dungeon d) {
        out.add("[PARTY]");
        for (Adventurer a : d.getAdventurers()) {
            List<Item> items = a.getInventory().list();
            out.add(row("ADV", clean(a.getName()), a.getPlayerClass().name(), a.getX(), a.getY(),
                    a.getHp(), a.getMana(), a.getGold(), a.getXp(), a.getAbilityCooldown(),
                    a.getReviveRoundsLeft(), a.isDead(), a.getEffects().getBlessedRounds(),
                    a.getEffects().getCursedRounds(),
                    items.indexOf(a.getWeapon()), items.indexOf(a.getArmor())));
            for (Item item : items) out.add("ITEM|" + ItemCodec.encode(item));
        }
    }

    private static String row(Object... fields) {
        return Arrays.stream(fields).map(String::valueOf).collect(Collectors.joining("|"));
    }

    private static String clean(String text) {
        return text.replace('|', '_').replace('\n', ' ');
    }
}
