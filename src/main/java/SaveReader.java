import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Rebuilds a game from save-file lines. Every problem becomes a SaveException. */
final class SaveReader {
    private static final int MAX_SIZE = 100;
    private static final int ADV_FIELDS = 16;

    private record Parts(Map<String, String> header, Map<String, List<String>> sections) { }

    private SaveReader() { }

    static SavedGame parse(List<String> lines) throws SaveException {
        if (lines.isEmpty() || !lines.get(0).startsWith("DUNGEONQUEST-SAVE")) {
            throw new SaveException("That is not a Dungeon Quest save file.");
        }
        Parts parts = split(lines);
        try {
            return build(parts);
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            throw new SaveException("The save file is damaged.");
        }
    }

    private static Parts split(List<String> lines) {
        Map<String, String> header = new HashMap<>();
        Map<String, List<String>> sections = new HashMap<>();
        List<String> current = null;
        for (String line : lines.subList(1, lines.size())) {
            if (line.isBlank()) continue;
            if (line.startsWith("[") && line.endsWith("]")) {
                current = new ArrayList<>();
                sections.put(line.substring(1, line.length() - 1), current);
            } else if (current != null) {
                current.add(line);
            } else if (line.contains("=")) {
                String[] kv = line.split("=", 2);
                header.put(kv[0].trim(), kv[1].trim());
            }
        }
        return new Parts(header, sections);
    }

    private static SavedGame build(Parts p) throws SaveException {
        int level = Integer.parseInt(need(p.header(), "level"));
        int[] size = ints(need(p.header(), "size"), 2);
        int[] start = ints(need(p.header(), "start"), 2);
        int[] dm = ints(need(p.header(), "dm"), 3);
        if (size[0] < 3 || size[1] < 3 || size[0] > MAX_SIZE || size[1] > MAX_SIZE) {
            throw new SaveException("The save file has an impossible map size.");
        }
        Dungeon d = new Dungeon(size[0], size[1]);
        d.setStart(start[0], start[1]);
        loadGrids(d, p.sections());
        loadMonsters(d, p.sections().getOrDefault("MONSTERS", List.of()));
        loadParty(d, p.sections().getOrDefault("PARTY", List.of()));
        return new SavedGame(level, d, dm[0], dm[1], dm[2]);
    }

    private static String need(Map<String, String> header, String key) throws SaveException {
        String value = header.get(key);
        if (value == null) throw new SaveException("The save file is missing '" + key + "'.");
        return value;
    }

    private static int[] ints(String text, int expected) throws SaveException {
        String[] parts = text.split(",");
        if (parts.length != expected) throw new SaveException("The save file header is damaged.");
        int[] out = new int[expected];
        for (int i = 0; i < expected; i++) out[i] = Integer.parseInt(parts[i].trim());
        return out;
    }

    // ---------- map ----------

    private static List<String> rows(Map<String, List<String>> s, String name, Dungeon d)
            throws SaveException {
        List<String> rows = s.get(name);
        if (rows == null || rows.size() != d.getHeight()) {
            throw new SaveException("The " + name + " section is missing or damaged.");
        }
        for (String r : rows) {
            if (r.length() != d.getWidth()) throw new SaveException("A " + name + " row is the wrong length.");
        }
        return rows;
    }

    private static void loadGrids(Dungeon d, Map<String, List<String>> s) throws SaveException {
        List<String> grid = rows(s, "GRID", d);
        List<String> explored = rows(s, "EXPLORED", d);
        List<String> revealed = rows(s, "REVEALED", d);
        for (int y = 0; y < d.getHeight(); y++) {
            for (int x = 0; x < d.getWidth(); x++) {
                RoomType type = RoomType.fromSymbol(grid.get(y).charAt(x));
                if (type == null) throw new SaveException("Unknown map symbol in the save file.");
                d.setRoom(x, y, type);
                if (explored.get(y).charAt(x) == '1') d.getRoom(x, y).markExplored();
                if (revealed.get(y).charAt(x) == '1') d.getRoom(x, y).reveal();
            }
        }
    }

    // ---------- monsters ----------

    private static void loadMonsters(Dungeon d, List<String> lines) throws SaveException {
        for (String line : lines) {
            String[] p = line.split("\\|", -1);
            if (p.length != 4) throw new SaveException("A monster entry is damaged.");
            Monster m = MonsterFactory.create(p[0])
                    .orElseThrow(() -> new SaveException("Unknown monster '" + p[0] + "'."));
            int x = Integer.parseInt(p[1]);
            int y = Integer.parseInt(p[2]);
            if (!d.inBounds(x, y)) throw new SaveException("A monster is outside the map.");
            m.moveTo(x, y);
            m.takeDamage(m.getMaxHp() - Integer.parseInt(p[3]));
            d.addMonster(m);
        }
    }

    // ---------- adventurers ----------

    private static void loadParty(Dungeon d, List<String> lines) throws SaveException {
        for (List<String> block : blocks(lines)) d.addAdventurer(readAdventurer(block, d));
        if (d.getAdventurers().isEmpty()) throw new SaveException("The save file has no adventurers.");
    }

    private static List<List<String>> blocks(List<String> lines) throws SaveException {
        List<List<String>> blocks = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("ADV|")) blocks.add(new ArrayList<>());
            if (blocks.isEmpty()) throw new SaveException("The PARTY section is damaged.");
            blocks.get(blocks.size() - 1).add(line);
        }
        return blocks;
    }

    private static Adventurer readAdventurer(List<String> block, Dungeon d) throws SaveException {
        String[] p = block.get(0).split("\\|", -1);
        if (p.length != ADV_FIELDS) throw new SaveException("An adventurer entry is damaged.");
        int x = Integer.parseInt(p[3]);
        int y = Integer.parseInt(p[4]);
        if (!d.inBounds(x, y)) throw new SaveException("An adventurer is outside the map.");
        Adventurer a = new Adventurer(p[1], PlayerClass.valueOf(p[2]), x, y);
        for (String line : block.subList(1, block.size())) {
            if (!line.startsWith("ITEM|")) throw new SaveException("An inventory entry is damaged.");
            a.getInventory().add(ItemCodec.decode(line.substring(5)));
        }
        equip(a, Integer.parseInt(p[14]), Integer.parseInt(p[15]));
        a.restoreXp(Integer.parseInt(p[8]));            // first: levels change max HP and mana
        a.restoreState(Integer.parseInt(p[5]), Integer.parseInt(p[6]),
                Integer.parseInt(p[10]), Boolean.parseBoolean(p[11]));
        a.addGold(Integer.parseInt(p[7]));
        a.startAbilityCooldown(Integer.parseInt(p[9]));
        a.getEffects().bless(Integer.parseInt(p[12]));
        a.getEffects().curse(Integer.parseInt(p[13]));
        return a;
    }

     static void equip(Adventurer a, int weaponSlot, int armorSlot) throws SaveException {
        if (weaponSlot >= 0 && a.getInventory().get(weaponSlot).orElse(null) instanceof Weapon w) {
            a.equipWeapon(w);
        }
        if (armorSlot >= 0 && a.getInventory().get(armorSlot).orElse(null) instanceof Armor armor) {
            a.equipArmor(armor);
        }
    }
}
