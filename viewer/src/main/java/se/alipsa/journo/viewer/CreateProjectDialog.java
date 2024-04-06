package se.alipsa.journo.viewer;


import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Map;
import java.util.Optional;

public class CreateProjectDialog extends Dialog<Map<String, String>> {


  public CreateProjectDialog() {
    setTitle("Create new Project");
    setHeaderText("Name of project");
    VBox root = new VBox();
    getDialogPane().setContent(root);
    HBox nameRow = new HBox();
    root.getChildren().add(nameRow);
    Label nameLabel = new Label("name");
    TextField nameField = new TextField();
    nameRow.getChildren().addAll(nameLabel, nameField);
    HBox locationRow = new HBox();
    root.getChildren().add(locationRow);
    Label locationLabel = new Label("location");
    TextField locationField = new TextField();
    Button locationButton = new Button("...");
    locationButton.setOnAction(a -> {
      FileChooser fc = new FileChooser();
      fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Journo project(*.jpr)", "*.jpr"));
      fc.setInitialFileName("*.jpr");
      File file = fc.showSaveDialog(null);
      if (file != null) {
        locationField.setText(ensureExtension(file.getAbsolutePath()));
      }
    });
    locationRow.getChildren().addAll(locationLabel, locationField, locationButton);
    ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    getDialogPane().getButtonTypes().addAll(cancelButtonType, createButtonType);
    getDialogPane().getStylesheets().add(JournoViewer.getStyleSheet().toExternalForm());
    Stage stage = (Stage) getDialogPane().getScene().getWindow();
    stage.getIcons().add(JournoViewer.getLogo());
    setResultConverter(c -> {
      if (c.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
        return Map.of("path", ensureExtension(locationField.getText()), "name", nameField.getText());
      }
      return null;
    });
  }

  String ensureExtension(String location) {
    if (location.endsWith(".jpr")) {
      location = location + ".jpr";
    }
    return location;
  }
}
