import javax.swing.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SetupScreen {

    private final Scanner in;
    private final Renderer renderer;
    private final HeroStore heroStore;

    public SetupScreen(
            Scanner in,
            Renderer renderer,
            HeroStore heroStore
    ) {
        this.in = in;
        this.renderer = renderer;
        this.heroStore = heroStore;
    }

    // ================= CHARACTER CREATION ENTRY POINT =================

    public List<Adventurer> createParty() {

        if (renderer instanceof SwingRenderer swing) {
            return createPartyGUI(swing);
        }

        return createPartyConsole();
    }

    // ================= GRAPHICAL CHARACTER CREATION =================

    private List<Adventurer> createPartyGUI(SwingRenderer swing) {

        List<Adventurer> party = new ArrayList<>();

        String[] counts = {"1", "2", "3", "4"};

        String selectedCount = (String) JOptionPane.showInputDialog(
                null,
                I18n.get("setup.partySize"),
                I18n.get("setup.title"),
                JOptionPane.QUESTION_MESSAGE,
                null,
                counts,
                counts[0]
        );

        // Player cancelled character creation.
        if (selectedCount == null) {
            return party;
        }

        int count = Integer.parseInt(selectedCount);

        // Create each adventurer.
        for (int i = 1; i <= count; i++) {

            String name = swing.askText(
                    MessageFormat.format(
                            I18n.get("setup.heroName"),
                            i
                    ),
                    I18n.get("setup.heroName")
            );

            // Cancel if the player provides no name.
            if (name == null || name.isBlank()) {

                JOptionPane.showMessageDialog(
                        null,
                        I18n.get("setup.cancelled"),
                        I18n.get("setup.title"),
                        JOptionPane.INFORMATION_MESSAGE
                );

                return new ArrayList<>();
            }

            // Retrieve the available player classes.
            PlayerClass[] classes = PlayerClass.values();

            String[] classNames = new String[classes.length];

            for (int j = 0; j < classes.length; j++) {

                classNames[j] = getTranslatedClassName(classes[j]);
            }

            // Ask the player to select a class.
            String selectedClass = (String) JOptionPane.showInputDialog(
                    null,
                    MessageFormat.format(
                            I18n.get("setup.chooseClass"),
                            name
                    ),
                    I18n.get("setup.title"),
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    classNames,
                    classNames[0]
            );

            if (selectedClass == null) {
                return new ArrayList<>();
            }

            // Convert the displayed class name back to PlayerClass.
            PlayerClass playerClass = null;

            for (PlayerClass pc : classes) {

                if (getTranslatedClassName(pc).equals(selectedClass)) {
                    playerClass = pc;
                    break;
                }
            }

            if (playerClass == null) {

                JOptionPane.showMessageDialog(
                        null,
                        I18n.get("setup.invalidChoice"),
                        I18n.get("setup.title"),
                        JOptionPane.ERROR_MESSAGE
                );

                return new ArrayList<>();
            }

            Adventurer adventurer = createAdventurer(
                    name.trim(),
                    playerClass
            );

            if (adventurer == null) {
                return new ArrayList<>();
            }

            party.add(adventurer);
        }

        // Confirm that the party is ready.
        // Confirm that the party is ready.
        JOptionPane.showMessageDialog(
                null,
                MessageFormat.format(
                        I18n.get("setup.partyReady"),
                        party.size()
                ),
                I18n.get("setup.title"),
                JOptionPane.INFORMATION_MESSAGE
        );

        return party;
    }

    // ================= CONSOLE CHARACTER CREATION =================

    private List<Adventurer> createPartyConsole() {

        List<Adventurer> party = new ArrayList<>();

        System.out.println("\n=== " + I18n.get("setup.title") + " ===");

        int count = askCountConsole();

        if (count == 0) {
            return party;
        }

        for (int i = 1; i <= count; i++) {

            System.out.print(
                    MessageFormat.format(
                            I18n.get("setup.heroName"),
                            i
                    ) + " "
            );

            if (!in.hasNextLine()) {
                return new ArrayList<>();
            }

            String name = in.nextLine().trim();

            if (name.isBlank()) {

                System.out.println(I18n.get("setup.invalidName"));

                return new ArrayList<>();
            }

            PlayerClass[] classes = PlayerClass.values();

            // Display translated class names.
            for (int j = 0; j < classes.length; j++) {

                System.out.println(
                        (j + 1) + ". " +
                                getTranslatedClassName(classes[j])
                );
            }

            System.out.print(I18n.get("setup.chooseOption") + " ");

            if (!in.hasNextLine()) {
                return new ArrayList<>();
            }

            int choice;

            try {

                choice = Integer.parseInt(in.nextLine().trim());

            } catch (NumberFormatException e) {

                System.out.println(I18n.get("setup.invalidChoice"));

                return new ArrayList<>();
            }

            if (choice < 1 || choice > classes.length) {

                System.out.println(I18n.get("setup.invalidChoice"));

                return new ArrayList<>();
            }

            Adventurer adventurer = createAdventurer(
                    name,
                    classes[choice - 1]
            );

            if (adventurer == null) {
                return new ArrayList<>();
            }

            party.add(adventurer);
        }

        return party;
    }

    // ================= PARTY SIZE VALIDATION =================

    private int askCountConsole() {

        while (true) {

            System.out.print(
                    I18n.get("setup.partySize") + " "
            );

            if (!in.hasNextLine()) {
                return 0;
            }

            try {

                int count = Integer.parseInt(in.nextLine().trim());

                if (count >= 1 && count <= 4) {
                    return count;
                }

            } catch (NumberFormatException ignored) {

                // Display an error and request input again.
            }

            System.out.println(I18n.get("setup.invalidNumber"));
        }
    }

    // ================= CLASS TRANSLATION =================

    private String getTranslatedClassName(PlayerClass playerClass) {

        return switch (playerClass) {

            case WARRIOR -> I18n.get("setup.warrior");

            case MAGE -> I18n.get("setup.mage");

            case ROGUE -> I18n.get("setup.rogue");

            case CLERIC -> I18n.get("setup.cleric");
        };
    }

    // ================= CHARACTER CREATION =================

    private Adventurer createAdventurer(
            String name,
            PlayerClass playerClass
    ) {

        if (name == null || name.isBlank()) {

            JOptionPane.showMessageDialog(
                    null,
                    I18n.get("setup.invalidName"),
                    I18n.get("setup.title"),
                    JOptionPane.ERROR_MESSAGE
            );

            return null;
        }

        try {

            return new Adventurer(name, playerClass);

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                    null,
                    MessageFormat.format(
                            I18n.get("setup.creationError"),
                            e.getMessage()
                    ),
                    I18n.get("setup.title"),
                    JOptionPane.ERROR_MESSAGE
            );

            return null;
        }
    }
}