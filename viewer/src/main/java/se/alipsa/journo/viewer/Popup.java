package se.alipsa.journo.viewer;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Popup {

  public static void display(Node img, JournoViewer gui, String... title) {
    Stage stage = new Stage();
    if (title.length > 0) {
      stage.setTitle(title[0]);
    }
    BorderPane pane = new BorderPane(img);
    pane.setPadding(new Insets(10));
    Scene scene = new Scene(pane);
    stage.getIcons().add(JournoViewer.getLogo());
    scene.getStylesheets().add(JournoViewer.getStyleSheet().toExternalForm());
    stage.setScene(scene);
    stage.show();
  }
}
