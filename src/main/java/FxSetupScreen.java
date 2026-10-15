import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class FxSetupScreen {

    private final Stage owner;

    public FxSetupScreen(Stage owner) {
        this.owner = owner;
    }

    public List<Adventurer> createParty() {

        List<Adventurer> party = new ArrayList<>();

        Stage dialog = new Stage();

        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Create Your Party");

        // Main layout.
        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);

        Label title = new Label("CREATE YOUR PARTY");
        title.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: gold;"
        );

        // Select the number of adventurers.
        Label countLabel = new Label("Number of adventurers:");

        ComboBox<Integer> countBox = new ComboBox<>();

        countBox.getItems().addAll(1, 2, 3, 4);
        countBox.setValue(1);

        // Character fields will be placed here.
        VBox characterContainer = new VBox(15);

        // Keep the form synchronized with party size.
        countBox.setOnAction(event -> {

            characterContainer.getChildren().clear();

            int count = countBox.getValue();

            for (int i = 1; i <= count; i++) {

                characterContainer.getChildren().add(
                        createCharacterForm(i)
                );
            }
        });

        // Create the initial character form.
        characterContainer.getChildren().add(
                createCharacterForm(1)
        );

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button startButton = new Button("START ADVENTURE");
        Button cancelButton = new Button("CANCEL");

        startButton.setMaxWidth(Double.MAX_VALUE);

        startButton.setOnAction(event -> {

            List<Adventurer> created = new ArrayList<>();

            for (int i = 0; i < characterContainer.getChildren().size(); i++) {

                GridPane form =
                        (GridPane) characterContainer.getChildren().get(i);

                TextField nameField =
                        (TextField) form.getChildren().get(2);

                @SuppressWarnings("unchecked")
                ComboBox<PlayerClass> classBox =
                        (ComboBox<PlayerClass>) form.getChildren().get(4);

                String name = nameField.getText().trim();

                PlayerClass playerClass = classBox.getValue();

                if (name.isBlank()) {

                    errorLabel.setText(
                            "Enter a name for adventurer " + (i + 1) + "."
                    );

                    return;
                }

                if (playerClass == null) {

                    errorLabel.setText(
                            "Select a class for adventurer " + (i + 1) + "."
                    );

                    return;
                }

                try {

                    created.add(
                            new Adventurer(name, playerClass)
                    );

                } catch (RuntimeException ex) {

                    errorLabel.setText(
                            "Could not create adventurer: " + ex.getMessage()
                    );

                    return;
                }
            }

            party.addAll(created);

            dialog.close();
        });

        cancelButton.setOnAction(event -> dialog.close());

        root.getChildren().addAll(
                title,
                countLabel,
                countBox,
                characterContainer,
                errorLabel,
                startButton,
                cancelButton
        );

        ScrollPane scrollPane = new ScrollPane(root);

        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 480, 600);

        dialog.setScene(scene);
        dialog.setMinWidth(400);
        dialog.setMinHeight(450);
        dialog.showAndWait();

        return party;
    }

    private GridPane createCharacterForm(int number) {

        GridPane form = new GridPane();

        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(12));

        form.setStyle(
                "-fx-background-color: #1e293b;" +
                        "-fx-background-radius: 8;"
        );

        Label heading = new Label("Adventurer " + number);

        heading.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-text-fill: gold;"
        );

        TextField nameField = new TextField();

        nameField.setPromptText("Enter character name");

        ComboBox<PlayerClass> classBox = new ComboBox<>();

        classBox.getItems().addAll(PlayerClass.values());

        classBox.setPromptText("Select class");

        classBox.setMaxWidth(Double.MAX_VALUE);

        form.add(heading, 0, 0, 2, 1);

        form.add(new Label("Name:"), 0, 1);
        form.add(nameField, 1, 1);

        form.add(new Label("Class:"), 0, 2);
        form.add(classBox, 1, 2);

        return form;
    }
}