package se.alipsa.journo.viewer;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.batik.transcoder.TranscoderException;

import java.io.File;
import java.io.IOException;

public class ExportPngDialog extends Dialog<File> {

  SvgTextArea svgTextArea;
  File exportFile;
  public ExportPngDialog(SvgTextArea svgTextArea, JournoViewer gui) {
    super();
    initOwner(gui.getStage());
    this.svgTextArea = svgTextArea;
    ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.APPLY);
    getDialogPane().getButtonTypes().addAll(cancel, save);
    getDialogPane().lookupButton(save).setDisable(true);

    setTitle("Export svg to png");
    BorderPane content = new BorderPane();
    VBox rowBox = new VBox();
    content.setCenter(rowBox);
    rowBox.setSpacing(5);
    HBox fileRow = new HBox();
    rowBox.getChildren().add(fileRow);
    fileRow.setAlignment(Pos.CENTER_LEFT);
    Label fileLabel = new Label("File ");
    TextField fileNameTf = new TextField();
    HBox.setHgrow(fileNameTf, Priority.ALWAYS);
    Button browseButton = new Button("...");
    fileRow.getChildren().addAll(fileLabel, fileNameTf, browseButton);
    browseButton.setOnAction(a -> {
      FileChooser fc = new FileChooser();
      fc.setTitle("PNG file name");
      exportFile = fc.showSaveDialog(gui.getStage());
      if (exportFile != null) {
        getDialogPane().lookupButton(save).setDisable(false);
        fileNameTf.setText(exportFile.getAbsolutePath());
      }
    });
    HBox scaleRow = new HBox();
    rowBox.getChildren().add(scaleRow);
    scaleRow.setAlignment(Pos.CENTER_LEFT);
    Label widthLabel = new Label("Width ");
    TextField widthTf = new TextField();
    Label heightLabel = new Label(" Height ");
    TextField heightTf = new TextField();
    scaleRow.getChildren().addAll(widthLabel, widthTf, heightLabel, heightTf);
    getDialogPane().setContent(content);
    getDialogPane().getStylesheets().add(JournoViewer.getStyleSheet().toExternalForm());
    Stage stage = (Stage) getDialogPane().getScene().getWindow();
    stage.getIcons().add(JournoViewer.getLogo());
    setResultConverter(c ->  {
      if (c == save) {
        try {
          if (widthTf.getText() == null || heightTf.getText() == null) {
            SvgImageExporter.svgToPng(svgTextArea.getText(), exportFile);
          } else {
            int width = Integer.parseInt(widthTf.getText());
            int height = Integer.parseInt(heightTf.getText());
            SvgImageExporter.svgToPng(svgTextArea.getText(), width, height, exportFile);
          }
          return exportFile;
        } catch (TranscoderException | IOException e) {
          ExceptionAlert.showAlert("Failed to export svg to " + exportFile, e);
        }
      }
      return null;
    });
  }
}
