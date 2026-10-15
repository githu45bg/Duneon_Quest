import java.util.Scanner;

public class DmConsole {
    private static final String HELP = """
            DM commands:
              trap <x> <y>      set a hidden trap on empty floor
              spawn <type>      goblin, skeleton, orc or dragon (appears out of sight)
              event [name]      random event, or: ambush, cave-in, blessing, curse, merchant
              narration <text>  speak to the table
              command <x> <y> <act|heal>  a monster takes an extra turn, or heals (uses an event)
              view              redraw the DM map
              help              show this list
              exit              leave the console
            """;

    private final DungeonMaster dm;
    private final Scanner in;
    private final Renderer renderer;

    public DungeonMaster getDm() {return dm;}

    public DmConsole(DungeonMaster dm, Scanner in, Renderer renderer) {
        this.dm = dm;
        this.in = in;
        this.renderer = renderer;
    }

    public void open(Dungeon d) {
        if (!dm.authenticate(in)) return;
        DmView.show(d);
        System.out.println(HELP + dm.limitsText());
        runLoop(d);
        System.out.print("\n".repeat(40));      // scroll the secret view away
    }

    private void runLoop(Dungeon d) {
        while (true) {
            System.out.print("DM > ");
            if (!in.hasNextLine()) return;
            String line = in.nextLine().trim();
            if (line.equalsIgnoreCase("exit")) return;
            if (line.equalsIgnoreCase("help")) System.out.println(HELP);
            else if (line.equalsIgnoreCase("view")) DmView.show(d);
            else runCommand(line, d);
        }
    }

    private void runCommand(String line, Dungeon d) {
        try {
            renderer.showMessage(dm.execute(line, d, in));
        } catch (CommandException e) {
            renderer.showMessage(e.getMessage());
        }
    }
}