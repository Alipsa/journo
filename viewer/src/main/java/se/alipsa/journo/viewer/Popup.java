package se.alipsa.journo.viewer;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Popup {

  public static void display(Node img, JournoViewer gui) {
    Stage stage = new Stage();
    BorderPane pane = new BorderPane(img);
    pane.setPadding(new Insets(10));
    Scene scene = new Scene(pane);
    stage.getIcons().add(gui.getAppIcon());
    stage.setScene(scene);
    stage.show();
  }
}
