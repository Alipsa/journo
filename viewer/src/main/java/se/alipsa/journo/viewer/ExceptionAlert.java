package se.alipsa.journo.viewer;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class ExceptionAlert extends Alert {

  private static Logger logger = LogManager.getLogger(ExceptionAlert.class);
  public ExceptionAlert() {
    super(AlertType.ERROR);
  }

  /**
   * log and display the exception.
   */
  public static Optional<ButtonType> showAlert(String message, Throwable throwable) {
    logger.error(message, throwable);
    Alert alert = new ExceptionAlert();
    alert.setTitle("Exception Dialog");
    alert.setHeaderText("An Exception Occurred");
    alert.setContentText(message);
    alert.setResizable(true);

    // Create expandable Exception.
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    throwable.printStackTrace(pw);
    String exceptionText = sw.toString();

    TextArea textArea = new TextArea();
    textArea.getStyleClass().add("txtarea");
    textArea.setText(exceptionText);
    textArea.setEditable(false);
    textArea.setWrapText(true);

    textArea.setMaxWidth(Double.MAX_VALUE);
    textArea.setMaxHeight(Double.MAX_VALUE);
    textArea.setMinHeight(Region.USE_PREF_SIZE);


    Label label = new Label("The exception stacktrace was:");
    GridPane expContent = new GridPane();
    GridPane.setVgrow(textArea, Priority.ALWAYS);
    GridPane.setHgrow(textArea, Priority.ALWAYS);

    expContent.setMaxWidth(Double.MAX_VALUE);
    expContent.add(label, 0, 0);
    expContent.add(textArea, 0, 1);
    expContent.setMinHeight(Region.USE_PREF_SIZE);

    // Set expandable Exception into the dialog pane.
    alert.getDialogPane().setExpandableContent(expContent);

    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    alert.getDialogPane().setPrefWidth(500);

    return alert.showAndWait();
  }

}
