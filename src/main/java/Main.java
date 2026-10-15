import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private void selectLanguage() {

        String[] languages = {
                "English",
                "Kiswahili"
        };

        int choice = JOptionPane.showOptionDialog(
                null,
                "Choose your preferred language / Chagua lugha:",
                "Dungeon Quest - Language",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                languages,
                languages[0]
        );

        if (choice == 1) {
            I18n.setLanguage("sw");
        } else {
            I18n.setLanguage("en");
        }
    }

    // Campaign level files
    private static final String[] CAMPAIGN = {
            "level1.txt",
            "level2.txt",
            "level3.txt"
    };

    // Randomly generated dungeon files
    private static final String[] GENERATED = {
            "generated1.txt",
            "generated2.txt",
            "generated3.txt"
    };

    // File and directory paths
    private static final Path LEVELS_DIR = Path.of("levels");
    private static final Path SCORES = Path.of("scores.txt");
    private static final Path HEROES_DIR = Path.of("characters");
    private static final Path DM_CONFIG =
            Path.of("config", "dm.properties");

    // Game components
    private final Renderer renderer;
    private final Scanner in = new Scanner(System.in);
    private final HeroStore heroStore = new HeroStore(HEROES_DIR);
    private final ScoreBoard scoreBoard = new ScoreBoard(SCORES);

    // Dungeon Master password
    private final String dmPassword =
            DmConfig.loadPassword(DM_CONFIG).orElse(null);

    // Current campaign levels
    private String[] levels = CAMPAIGN;

    public Main() {
        this(new SwingRenderer());
    }

    public Main(Renderer renderer) {
        this.renderer = renderer;
    }


    //  entry point
    public static void main(String[] args) {

        // Ask the player to choose a language.
        Main game = new Main();
        game.selectLanguage();

        // Temporary sound test.
        // Remove this line after confirming sound works.
        Sound.testSound();

        // Launch the main menu.
        game.menu();
    }

    private void showHighScores() {
        JOptionPane.showMessageDialog(
                null,
                scoreBoard.topScores(10),
                "Dungeon Quest - High Scores",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    // Main menu
    private void menu() {

        while (true) {

            String[] options = {
                    I18n.get("menu.newGame"),
                    I18n.get("menu.randomDungeon"),
                    I18n.get("menu.loadGame"),
                    I18n.get("menu.highScores"),
                    I18n.get("menu.quit")
            };

            int choice = JOptionPane.showOptionDialog(
                    null,
                    I18n.get("menu.title"),
                    I18n.get("app.title"),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            switch (choice) {

                case 0 -> newGame(CAMPAIGN);

                case 1 -> randomGame();

                case 2 -> loadGame();

                case 3 -> showHighScores();

                case 4, -1 -> {
                    System.exit(0);
                    return;
                }

                default -> {
                    return;
                }
            }
        }
    }


    // Display the high scores
    private void showHighScoreMains() {

        JOptionPane.showMessageDialog(
                null,
                scoreBoard.topScores(10).toString(),
                "High Scores",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    // Generate and start a random dungeon
    private void randomGame() {

        try {

            LevelGenerator.writeCampaign(
                    LEVELS_DIR,
                    new Random()
            );

            newGame(GENERATED);

        } catch (IOException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Could not generate the dungeon:\n"
                            + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    // Start a new game
    private void newGame(String[] levelFiles) {

        List<Adventurer> party =
                new SetupScreen(in, renderer, heroStore).createParty();

        if (party.isEmpty()) {
            return;
        }

        renderer.showMessage(
                dmPassword == null
                        ? "Dungeon Master mode is disabled."
                        : "Dungeon Master mode is available."
        );

        levels = levelFiles;

        playCampaign(
                party,
                new DungeonMaster(dmPassword),
                0,
                null
        );
    }


    // Load a previously saved game
    private void loadGame() {

        List<String> saves = GameSaver.listSaves();

        if (saves.isEmpty()) {

            JOptionPane.showMessageDialog(
                    null,
                    "No saved games found.",
                    "Load Game",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        String saveName = (String) JOptionPane.showInputDialog(
                null,
                "Choose a saved game:",
                "Load Game",
                JOptionPane.QUESTION_MESSAGE,
                null,
                saves.toArray(),
                saves.get(0)
        );

        if (saveName == null) {
            return;
        }

        try {

            SavedGame saved = GameSaver.load(saveName);

            if (saved.levelIndex() < 0
                    || saved.levelIndex() >= CAMPAIGN.length) {

                throw new SaveException(
                        "The saved game refers to an invalid level."
                );
            }

            DungeonMaster dm = new DungeonMaster(
                    dmPassword,
                    saved.traps(),
                    saved.spawns(),
                    saved.events()
            );

            List<Adventurer> party =
                    new ArrayList<>(saved.dungeon().getAdventurers());

            levels = CAMPAIGN;

            playCampaign(
                    party,
                    dm,
                    saved.levelIndex(),
                    saved.dungeon()
            );

        } catch (SaveException e) {

            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    // Run the campaign level by level
    private void playCampaign(
            List<Adventurer> party,
            DungeonMaster dm,
            int startLevel,
            Dungeon resumed
    ) {

        DmConsole console = new DmConsole(dm, in, renderer);

        List<Adventurer> everyone = new ArrayList<>(party);

        Dungeon dungeon = resumed;

        for (int level = startLevel;
             level < levels.length;
             level++) {

            // Load the level if this is not a resumed game
            if (dungeon == null) {
                dungeon = startLevel(level, party);
            }

            if (dungeon == null) {
                return;
            }

            renderer.showMessage(
                    "=== LEVEL " + (level + 1) + " ==="
            );

            Game game = new Game(
                    dungeon,
                    renderer,
                    in,
                    console,
                    level
            );

            // Start gameplay
            game.run();

            if (game.wasQuit()) {
                return;
            }

            // Stop the campaign if the party did not win
            if (!game.won()) {

                finish(
                        everyone,
                        level + 1,
                        false
                );

                return;
            }

            // Prepare the surviving party for the next level
            prepareNextLevel(party);

            dungeon = null;
        }

        // The campaign is complete
        finish(
                everyone,
                levels.length,
                true
        );
    }


    // Load an individual dungeon level
    private Dungeon startLevel(
            int level,
            List<Adventurer> party
    ) {

        try {

            Dungeon dungeon = LevelLoader.load(
                    LEVELS_DIR.resolve(levels[level])
            );

            PartyPlacer.place(dungeon, party);

            return dungeon;

        } catch (LevelLoadException e) {

            renderer.showMessage(
                    "Error loading level: " + e.getMessage()
            );

            return null;
        }
    }


    // Finish the game and record the results
    private void finish(
            List<Adventurer> everyone,
            int level,
            boolean victory
    ) {

        renderer.showMessage(
                victory
                        ? "*** You have conquered Dungeon Quest! ***"
                        : "*** The adventure ends here. ***"
        );

        if (!scoreBoard.append(everyone, level, victory)) {

            renderer.showMessage(
                    heroStore.saveAll(everyone)
            );
        }

        JOptionPane.showMessageDialog(
                null,
                scoreBoard.topScores(5).toString(),
                "Final Scores",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    // Prepare adventurers for the next level
    private void prepareNextLevel(List<Adventurer> party) {

        // Remove dead adventurers
        party.removeIf(Adventurer::isDead);

        for (Adventurer a : party) {

            // Recover unconscious adventurers
            if (a.isUnconscious()) {
                a.heal(1);
            }

            // Restore some health and mana
            a.heal(a.getMaxHp() / 4);
            a.restoreMana(5);
        }
    }

}