import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

public class FxLauncher extends Application {

    @Override
    public void start(Stage stage) {

        // Game title
        Label title = new Label("DUNGEON QUEST");

        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 42));
        title.setTextFill(Color.GOLD);

        // Subtitle
        Label subtitle = new Label("Enter the dungeon. Survive the darkness.");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setTextFill(Color.LIGHTGRAY);

        // Menu buttons
        Button playButton = createButton("NEW GAME");
        Button loadButton = createButton("LOAD GAME");
        Button settingsButton = createButton("SETTINGS");
        Button exitButton = createButton("EXIT");

        // Temporary button actions
        playButton.setOnAction(event -> {

            FxSetupScreen setupScreen = new FxSetupScreen(stage);

            List<Adventurer> party = setupScreen.createParty();

            if (party.isEmpty()) {
                return;
            }

            FxGameController controller=
                    new FxGameController(stage);
            controller.startNewGame(party);

        });

        loadButton.setOnAction(event ->
                showMessage(stage, "Load Game", "Loading will be implemented next.")
        );

        settingsButton.setOnAction(event ->
                showMessage(stage, "Settings", "Settings will be implemented next.")
        );

        exitButton.setOnAction(event -> stage.close());

        // Menu layout
        VBox menu = new VBox(
                18,
                title,
                subtitle,
                playButton,
                loadButton,
                settingsButton,
                exitButton
        );

        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(35));

        // Dark fantasy background
        menu.setStyle("""
                -fx-background-color:
                    linear-gradient(to bottom, #111827, #020617);
                """);

        // Window scene
        Scene scene = new Scene(menu, 1000, 650);

        stage.setTitle("Dungeon Quest — JavaFX");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(550);
        stage.setResizable(true);

        stage.show();
    }

    private Button createButton(String text) {

        Button button = new Button(text);

        button.setPrefWidth(260);
        button.setPrefHeight(48);

        button.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        button.setStyle("""
                -fx-background-color: #1e293b;
                -fx-text-fill: #f8fafc;
                -fx-border-color: #64748b;
                -fx-border-width: 1;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-cursor: hand;
                """);

        // Hover effect
        button.setOnMouseEntered(event ->
                button.setStyle("""
                        -fx-background-color: #b45309;
                        -fx-text-fill: white;
                        -fx-border-color: #fbbf24;
                        -fx-border-width: 1;
                        -fx-background-radius: 8;
                        -fx-border-radius: 8;
                        -fx-cursor: hand;
                        """
                )
        );

        button.setOnMouseExited(event ->
                button.setStyle("""
                        -fx-background-color: #1e293b;
                        -fx-text-fill: #f8fafc;
                        -fx-border-color: #64748b;
                        -fx-border-width: 1;
                        -fx-background-radius: 8;
                        -fx-border-radius: 8;
                        -fx-cursor: hand;
                        """
                )
        );

        return button;
    }

    private void showMessage(Stage owner, String title, String message) {

        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION
                );

        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
