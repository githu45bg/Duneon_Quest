import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DungeonMaster {
    private static final int MAX_TRAPS = 3;
    private static final int MAX_SPAWNS = 3;
    private static final int MAX_EVENTS = 3;
    private static final int MAX_ATTEMPTS = 3;

    private final String password;       // null = console disabled
    private int trapsLeft;
    private int spawnsLeft;
    private int eventsLeft;

    public int getTrapsLeft()  { return trapsLeft; }
    public int getSpawnsLeft() { return spawnsLeft; }
    public int getEventsLeft() { return eventsLeft; }

    public DungeonMaster(String password) {
        this(password, MAX_TRAPS, MAX_SPAWNS, MAX_EVENTS);
    }

    public DungeonMaster(String password, int traps, int spawns, int events) {
        this.password = password;
        this.trapsLeft = traps;
        this.spawnsLeft = spawns;
        this.eventsLeft = events;
    }

    public boolean authenticate(Scanner in) {
        if (password == null) {
            System.out.println("DM console unavailable: no password in config/dm.properties.");
            return false;
        }
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            System.out.print("DM password: ");
            if (!in.hasNextLine()) return false;
            if (password.equals(in.nextLine().trim())) return true;
            System.out.println("Wrong password.");
        }
        System.out.println("Too many attempts.");
        return false;
    }

    public String limitsText() {
        return "Traps left: " + trapsLeft + "  Spawns left: " + spawnsLeft
                + "  Events left: " + eventsLeft;
    }

    public String execute(String line, Dungeon d, Scanner in) throws CommandException {
        String[] parts = line.trim().split("\\s+", 2);
        String arg = parts.length > 1 ? parts[1] : "";
        return switch (parts[0].toLowerCase()) {
            case "trap"      -> trap(arg, d);
            case "spawn"     -> spawn(arg, d);
            case "event"     -> event(arg, d, in);
            case "narration" -> narrate(arg);
            case "command"   -> command(arg, d);
            default -> throw new CommandException("Unknown DM command. Type 'help'.");
        };
    }

    private String trap(String arg, Dungeon d) throws CommandException {
        if (trapsLeft == 0) throw new CommandException("No traps left this game.");
        String[] p = arg.split("\\s+");
        if (p.length != 2) throw new CommandException("Usage: trap <x> <y>");
        int x = number(p[0]);
        int y = number(p[1]);
        if (!d.inBounds(x, y) || d.getRoom(x, y).getType() != RoomType.FLOOR
                || d.isOccupied(x, y)) {
            throw new CommandException("Traps can only be set on empty floor.");
        }
        d.setRoom(x, y, RoomType.TRAP);
        trapsLeft--;
        return "Trap set at (" + x + "," + y + "). Traps left: " + trapsLeft;
    }

    private String spawn(String arg, Dungeon d) throws CommandException {
        if (spawnsLeft == 0) throw new CommandException("No spawns left this game.");
        Monster m = MonsterFactory.create(arg).orElseThrow(() -> new CommandException(
                "Unknown monster. Try: goblin, skeleton, orc, dragon."));
        int[] spot = hiddenSpot(d).orElseThrow(() -> new CommandException(
                "No free hidden cell to spawn into."));
        m.moveTo(spot[0], spot[1]);
        d.addMonster(m);
        spawnsLeft--;
        return m.getName() + " lurks somewhere out of sight. Spawns left: " + spawnsLeft;
    }
    private String command(String arg, Dungeon d) throws CommandException {
        if (eventsLeft == 0) throw new CommandException("No events left this game.");
        String[] p = arg.split("\\s+");
        if (p.length != 3) throw new CommandException("Usage: command <x> <y> <act|heal>");
        int x = number(p[0]);
        int y = number(p[1]);
        Monster m = d.getMonsters().stream()
                .filter(mon -> mon.isAlive() && mon.getX() == x && mon.getY() == y)
                .findFirst()
                .orElseThrow(() -> new CommandException("There is no monster at (" + x + "," + y + ")."));
        String result = MonsterCommands.perform(p[2], m, d);
        eventsLeft--;
        return result + " Events left: " + eventsLeft;
    }

    private String event(String arg, Dungeon d, Scanner in) throws CommandException {
        if (eventsLeft == 0) throw new CommandException("No events left this game.");
        Optional<Event> chosen = arg.isBlank()
                ? Optional.of(EventFactory.random()) : EventFactory.byName(arg);
        Event e = chosen.orElseThrow(() -> new CommandException(
                "Unknown event. Try: ambush, cave-in, blessing, curse, merchant."));
        eventsLeft--;
        return "[" + e.getName() + "] " + e.trigger(d, in);
    }

    private String narrate(String arg) throws CommandException {
        if (arg.isBlank()) throw new CommandException("Usage: narration <text>");
        return "The Dungeon Master: " + arg;
    }

    private int number(String s) throws CommandException {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new CommandException("Coordinates must be whole numbers.");
        }
    }

    private Optional<int[]> hiddenSpot(Dungeon d) {
        List<int[]> spots = new ArrayList<>();
        for (int y = 0; y < d.getHeight(); y++) {
            for (int x = 0; x < d.getWidth(); x++) {
                boolean unseen = !FieldOfView.partyCanSee(d, x, y, FieldOfView.SIGHT_RADIUS);
                if (unseen && d.canEnter(x, y)) spots.add(new int[] {x, y});
            }
        }
        return spots.isEmpty() ? Optional.empty() : Optional.of(Dice.pick(spots));
    }
}
