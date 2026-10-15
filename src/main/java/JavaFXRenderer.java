import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class JavaFXRenderer implements Renderer {

    private final Stage stage;

    private final GridPane mapGrid = new GridPane();

    private final Label statusLabel =
            new Label("Welcome to Dungeon Quest");

    private final VBox messageBox = new VBox(5);

    private final ScrollPane messageScroll =
            new ScrollPane(messageBox);

    private final Map<String, Color> roomColors =
            new HashMap<>();

    private static final int TILE_SIZE = 30;

    public JavaFXRenderer(Stage stage) {
        this.stage = stage;

        roomColors.put("WALL", Color.web("#263244"));
        roomColors.put("FLOOR", Color.web("#a8a29e"));
        roomColors.put("TREASURE", Color.web("#eab308"));
        roomColors.put("POTION", Color.web("#ef4444"));
        roomColors.put("KEY", Color.web("#facc15"));
        roomColors.put("CHEST", Color.web("#a16207"));
        roomColors.put("EXIT", Color.web("#16a34a"));
        roomColors.put("TRAP", Color.web("#dc2626"));
        roomColors.put("SECRET", Color.web("#7c3aed"));
    }

    public void showGameScreen() {

        Label title = new Label("DUNGEON QUEST");

        title.setFont(
                Font.font("Arial", FontWeight.BOLD, 24)
        );

        title.setTextFill(Color.GOLD);

        statusLabel.setTextFill(Color.WHITE);
        statusLabel.setFont(Font.font("Arial", 14));

        statusLabel.setPadding(new Insets(8));

        mapGrid.setAlignment(Pos.CENTER);
        mapGrid.setHgap(1);
        mapGrid.setVgap(1);

        messageBox.setPadding(new Insets(10));
        messageBox.setStyle(
                "-fx-background-color: #111827;"
        );

        messageScroll.setFitToWidth(true);
        messageScroll.setPrefHeight(250);

        Label messagesTitle = new Label("GAME MESSAGES");
        messagesTitle.setTextFill(Color.GOLD);
        messagesTitle.setFont(
                Font.font("Arial", FontWeight.BOLD, 15)
        );

        VBox rightPanel = new VBox(
                10,
                messagesTitle,
                messageScroll
        );

        rightPanel.setPadding(new Insets(15));
        rightPanel.setPrefWidth(270);
        rightPanel.setStyle(
                "-fx-background-color: #111827;"
        );

        VBox top = new VBox(5, title, statusLabel);

        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(12));

        BorderPane root = new BorderPane();

        root.setTop(top);
        root.setCenter(mapGrid);
        root.setRight(rightPanel);

        root.setStyle(
                "-fx-background-color: #0f172a;"
        );

        BorderPane.setMargin(mapGrid, new Insets(12));

        Scene scene = new Scene(root, 1150, 800);

        stage.setTitle("Dungeon Quest — Gameplay");
        stage.setScene(scene);
        stage.setMinWidth(850);
        stage.setMinHeight(600);
        stage.show();
    }

    @Override
    public void render(Dungeon dungeon) {

        if (dungeon == null) {
            return;
        }

        Platform.runLater(() -> {

            mapGrid.getChildren().clear();

            int width = dungeon.getWidth();
            int height = dungeon.getHeight();

            for (int y = 0; y < height; y++) {

                for (int x = 0; x < width; x++) {

                    Room room = dungeon.getRoom(x, y);

                    Label tile = new Label();

                    tile.setMinSize(TILE_SIZE, TILE_SIZE);
                    tile.setPrefSize(TILE_SIZE, TILE_SIZE);
                    tile.setMaxSize(TILE_SIZE, TILE_SIZE);

                    tile.setAlignment(Pos.CENTER);

                    tile.setFont(
                            Font.font("Consolas",
                                    FontWeight.BOLD, 15)
                    );

                    tile.setTextFill(Color.WHITE);

                    if (!room.isExplored()
                            && !room.isRevealed()) {

                        tile.setText("");
                        tile.setStyle(
                                "-fx-background-color: #020617;"
                        );

                    } else {

                        RoomType type = room.visibleType();

                        Color color = roomColors.getOrDefault(
                                type.name(),
                                Color.web("#78716c")
                        );

                        tile.setStyle(
                                "-fx-background-color: "
                                        + toHex(color) + ";"
                        );

                        tile.setText(
                                symbolFor(type.name())
                        );
                    }

                    // Display a living monster on the tile.
                    for (Monster monster : dungeon.getMonsters()) {

                        if (monster.isAlive()
                                && monster.getX() == x
                                && monster.getY() == y) {

                            tile.setText("M");
                            tile.setTextFill(Color.ORANGERED);
                            break;
                        }
                    }

                    // Display an adventurer on the tile.
                    for (Adventurer adventurer
                            : dungeon.getAdventurers()) {

                        if (!adventurer.isDead()
                                && adventurer.getX() == x
                                && adventurer.getY() == y) {

                            tile.setText("@");
                            tile.setTextFill(Color.LIGHTSKYBLUE);
                            break;
                        }
                    }

                    mapGrid.add(tile, x, y);
                }
            }

            statusLabel.setText(
                    "Dungeon: " + width + " × " + height
                            + " | Adventurers: "
                            + dungeon.livingAdventurers().size()
                            + " | Monsters: "
                            + dungeon.getMonsters().stream()
                            .filter(Monster::isAlive)
                            .count()
            );
        });
    }

    private String symbolFor(String roomType) {

        return switch (roomType) {
            case "WALL" -> "#";
            case "FLOOR" -> ".";
            case "TREASURE" -> "T";
            case "POTION" -> "P";
            case "KEY" -> "K";
            case "CHEST" -> "C";
            case "EXIT" -> "E";
            case "TRAP" -> "^";
            case "SECRET" -> "H";
            default -> "?";

        };
    }

    private String toHex(Color color) {

        return String.format(
                "#%02X%02X%02X",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255)
        );
    }

    @Override
    public void showMessage(String message) {

        Platform.runLater(() -> {

            Label label = new Label(message);

            label.setTextFill(Color.WHITE);
            label.setWrapText(true);
            label.setMaxWidth(240);

            messageBox.getChildren().add(label);

            messageScroll.setVvalue(1.0);
        });
    }
}