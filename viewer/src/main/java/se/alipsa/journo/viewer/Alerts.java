package se.alipsa.journo.viewer;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

import java.util.Optional;

public class Alerts {


  public static  Optional<ButtonType> info(String title, String content) {
    return showAlert(title, content, Alert.AlertType.INFORMATION);
  }

  public static void infoFx(String title, String content) {
    showAlertFx(title, content, Alert.AlertType.INFORMATION);
  }

  public static  Optional<ButtonType> warn(String title, String content) {
    return showAlert(title, content, Alert.AlertType.WARNING);
  }

  public static void warnFx(String title, String content) {
    showAlertFx(title, content, Alert.AlertType.WARNING);
  }

  public static Optional<ButtonType> showAlert(String title, String content, Alert.AlertType information) {

      TextArea textArea = new TextArea(content);
      textArea.setEditable(false);
      textArea.setWrapText(true);

      BorderPane pane = new BorderPane();
      pane.setCenter(textArea);

      Alert alert = new Alert(information);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.getDialogPane().setContent(pane);
      alert.setResizable(true);

      return alert.showAndWait();
  }

  public static void showAlertFx(String title, String content, Alert.AlertType information) {
    Platform.runLater(() -> showAlert(title, content, information));
  }
}
