import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Keeps heroes between campaigns: name, class, XP (and so level), gold and gear.
 * One small text file per hero, e.g. characters/brian.txt.
 */
public class HeroStore {
    private static final String HEADER = "DUNGEONQUEST-HERO 1";

    private final Path folder;

    public HeroStore(Path folder) {
        this.folder = folder;
    }

    public boolean exists(String name) {
        try {
            return Files.isRegularFile(fileFor(name));
        } catch (SaveException e) {
            return false;
        }
    }

    public void save(Adventurer hero) throws SaveException {
        Path file = fileFor(hero.getName());
        try {
            Files.createDirectories(folder);
            Files.write(file, lines(hero));
        } catch (IOException e) {
            throw new SaveException("Could not save the hero " + hero.getName() + ".");
        }
    }

    /** Saves everyone in the party. Returns a message to show the players. */
    public String saveAll(List<Adventurer> heroes) {
        int saved = 0;
        for (Adventurer hero : heroes) {
            try {
                save(hero);
                saved++;
            } catch (SaveException e) {
                return e.getMessage();
            }
        }
        return saved + " hero(es) saved in " + folder + ", ready for the next campaign.";
    }

    /** Returns empty if this hero has never been saved. */
    public Optional<Adventurer> load(String name) throws SaveException {
        Path file = fileFor(name);
        if (!Files.isRegularFile(file)) return Optional.empty();
        try {
            return Optional.of(parse(Files.readAllLines(file)));
        } catch (IOException e) {
            throw new SaveException("Could not read the saved hero '" + name + "'.");
        }
    }

    /** One line of facts for the setup screen, e.g. "MAGE, level 3, 300 XP, 120 gold, 2 item(s)". */
    public static String describe(Adventurer hero) {
        return hero.getPlayerClass() + ", level " + hero.getLevel() + ", " + hero.getXp() + " XP, "
                + hero.getGold() + " gold, " + hero.getInventory().list().size() + " item(s)";
    }

    // ---------- writing ----------

    private List<String> lines(Adventurer hero) {
        List<Item> items = hero.getInventory().list();
        List<String> out = new ArrayList<>();
        out.add(HEADER);
        out.add("name=" + hero.getName());
        out.add("class=" + hero.getPlayerClass().name());
        out.add("xp=" + hero.getXp());
        out.add("gold=" + hero.getGold());
        out.add("weapon=" + items.indexOf(hero.getWeapon()));
        out.add("armor=" + items.indexOf(hero.getArmor()));
        for (Item item : items) out.add("ITEM|" + ItemCodec.encode(item));
        return out;
    }

    // ---------- reading ----------

    private Adventurer parse(List<String> lines) throws SaveException {
        if (lines.isEmpty() || !lines.get(0).equals(HEADER)) {
            throw new SaveException("That is not a Dungeon Quest hero file.");
        }
        Map<String, String> fields = new HashMap<>();
        List<String> items = new ArrayList<>();
        for (String line : lines.subList(1, lines.size())) {
            if (line.startsWith("ITEM|")) {
                items.add(line.substring(5));
            } else if (line.contains("=")) {
                String[] pair = line.split("=", 2);
                fields.put(pair[0].trim(), pair[1].trim());
            }
        }
        try {
            return build(fields, items);
        } catch (IllegalArgumentException e) {
            throw new SaveException("The saved hero file is damaged.");
        }
    }

    private Adventurer build(Map<String, String> f, List<String> items) throws SaveException {
        Adventurer hero = new Adventurer(need(f, "name"), PlayerClass.valueOf(need(f, "class")));
        for (String text : items) hero.getInventory().add(ItemCodec.decode(text));
        SaveReader.equip(hero, Integer.parseInt(need(f, "weapon")), Integer.parseInt(need(f, "armor")));
        hero.restoreXp(Integer.parseInt(need(f, "xp")));
        hero.addGold(Integer.parseInt(need(f, "gold")));
        return hero;
    }

    private String need(Map<String, String> fields, String key) throws SaveException {
        String value = fields.get(key);
        if (value == null || value.isBlank()) throw new SaveException("The hero file is missing '" + key + "'.");
        return value;
    }

    /** Only simple names become file names, so nothing is written outside the folder. */
    private Path fileFor(String name) throws SaveException {
        String key = name.trim().toLowerCase().replace(' ', '_');
        if (!key.matches("[a-z0-9_]{1,12}")) {
            throw new SaveException("Hero names can use letters, digits and spaces (up to 12).");
        }
        return folder.resolve(key + ".txt");
    }
}