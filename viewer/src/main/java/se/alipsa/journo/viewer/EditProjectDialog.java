package se.alipsa.journo.viewer;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Paths;
import java.util.Map;

public class EditProjectDialog extends Dialog<Boolean> {

  public EditProjectDialog(Project p) {
    setTitle("Edit Project " + p.getName());
    VBox root = new VBox();
    root.setSpacing(5);
    getDialogPane().setContent(root);

    HBox nameRow = new HBox();
    root.getChildren().add(nameRow);
    Label nameLabel = new Label("name ");
    TextField nameField = new TextField(p.getName());
    HBox.setHgrow(nameField, Priority.ALWAYS);
    nameRow.getChildren().addAll(nameLabel, nameField);

    HBox templateRow = new HBox();
    root.getChildren().add(templateRow);
    Label templateLabel = new Label("template ");
    TextField templateField = new TextField(String.valueOf(p.getTemplateFile()));
    HBox.setHgrow(templateField, Priority.ALWAYS);
    templateRow.getChildren().addAll(templateLabel, templateField);

    HBox scriptRow = new HBox();
    root.getChildren().add(scriptRow);
    Label scriptLabel = new Label("script ");
    TextField scriptField = new TextField(String.valueOf(p.getDataFile()));
    HBox.setHgrow(scriptField, Priority.ALWAYS);
    scriptRow.getChildren().addAll(scriptLabel, scriptField);

    ButtonType createButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    getDialogPane().getButtonTypes().addAll(cancelButtonType, createButtonType);
    getDialogPane().getStylesheets().add(JournoViewer.getStyleSheet().toExternalForm());
    Stage stage = (Stage) getDialogPane().getScene().getWindow();
    stage.getIcons().add(JournoViewer.getLogo());
    setResultConverter(c -> {
      if (c.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
        p.setName(nameField.getText());
        p.setDataFile(Paths.get(scriptField.getText()));
        p.setTemplateFile(Paths.get(templateField.getText()));
        return Boolean.TRUE;
      }
      return Boolean.FALSE;
    });
    setResizable(true);
    getDialogPane().setPrefWidth(600);
  }
}
