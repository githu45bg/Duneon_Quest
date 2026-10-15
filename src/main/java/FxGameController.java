import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class FxGameController {

    private static final Path LEVELS_DIR = Path.of("levels");

    private static final String[] CAMPAIGN = {
            "level1.txt",
            "level2.txt",
            "level3.txt"
    };

    private final Stage stage;

    private final BlockingQueue<String> commandQueue =
            new LinkedBlockingQueue<>();

    private JavaFXRenderer renderer;
    private Thread gameThread;

    public FxGameController(Stage stage) {
        this.stage = stage;
    }


    public void startNewGame(List<Adventurer> party) {

        if (party == null || party.isEmpty()) {
            return;
        }

        renderer = new JavaFXRenderer(stage);
        renderer.showGameScreen();

        // Start gameplay outside the JavaFX application thread.
        gameThread = new Thread(
                () -> playCampaign(party),
                "DungeonQuest-GameThread"
        );

        gameThread.setDaemon(true);
        gameThread.start();
    }

    private void playCampaign(List<Adventurer> party) {

        try (Scanner scanner = new Scanner("")) {

            for (int level = 0; level < CAMPAIGN.length; level++) {

                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                final int levelIndex = level;

                renderer.showMessage(
                        "Loading dungeon level " + (level + 1) + "..."
                );

                Dungeon dungeon = LevelLoader.load(
                        LEVELS_DIR.resolve(CAMPAIGN[level])
                );

                PartyPlacer.place(dungeon, party);

                // Temporary placeholder: replace with your actual
                // Dungeon Master configuration when confirmed.
                DungeonMaster dungeonMaster =  createDungeonMaster();

                DmConsole dmConsole = new DmConsole(
                        dungeonMaster,
                        scanner,
                        renderer
                );

                Game game = new Game(
                        dungeon,
                        renderer,
                        scanner,
                        dmConsole,
                        levelIndex,
                        this::getCommand
                );

                game.run();

                if (game.wasQuit()) {
                    renderer.showMessage("You left the dungeon.");
                    return;
                }

                if (!game.won()) {
                    renderer.showMessage("Your adventure has ended.");
                    return;
                }

                renderer.showMessage(
                        "You completed level " + (levelIndex + 1) + "!"
                );

                if (level < CAMPAIGN.length - 1) {
                    renderer.showMessage(
                            "Preparing the next level..."
                    );
                }
            }

            renderer.showMessage(
                    "Congratulations! You completed the campaign!"
            );

        } catch (Exception e) {

            Platform.runLater(() -> renderer.showMessage(
                    "Could not start the game: " + e.getMessage()
            ));

            e.printStackTrace();
        }
    }

    private DungeonMaster createDungeonMaster() {

        Path configFile = Path.of("config", "dm.properties");
        Properties properties = new Properties();

        if (!Files.exists(configFile)) {
            renderer.showMessage(
                    "Dungeon Master configuration not found. DM mode disabled."
            );
            return new DungeonMaster(null);
        }

        try (var input = Files.newInputStream(configFile)) {

            properties.load(input);

            String password = properties.getProperty("password");

            if (password == null || password.isBlank()) {
                renderer.showMessage(
                        "Dungeon Master password is missing. DM mode disabled."
                );
                return new DungeonMaster(null);
            }

            return new DungeonMaster(password);

        } catch (IOException e) {

            renderer.showMessage(
                    "Could not read DM configuration: " + e.getMessage()
            );

            return new DungeonMaster(null);
        }
    }

    private String getCommand(String adventurerName) {

        BlockingQueue<Optional<String>> resultQueue =
                new LinkedBlockingQueue<>(1);

        Platform.runLater(() -> {

            TextInputDialog dialog = new TextInputDialog();

            dialog.initOwner(stage);
            dialog.setTitle("Dungeon Quest - Your Turn");
            dialog.setHeaderText(
                    adventurerName + "'s turn"
            );
            dialog.setContentText(
                    "Enter a command:\n"
                            + "Examples: MOVE N, ATTACK, H, I, Q"
            );

            Optional<String> result = dialog.showAndWait();

            resultQueue.offer(result);
        });

        try {

            Optional<String> result = resultQueue.take();

            return result.orElse("Q");

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            return "Q";
        }
    }

    public void submitCommand(String command) {

        if (command != null && !command.isBlank()) {
            commandQueue.offer(command.trim());
        }
    }
}