package se.alipsa.journo.viewer;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.girod.javafx.svgimage.SVGImage;
import org.girod.javafx.svgimage.SVGLoader;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class SvgTab extends JournoTab {
  private final SvgTextArea textArea;
  private File svgFile = null;
  public SvgTab(JournoViewer gui) {
    super(gui);
    BorderPane root = new BorderPane();
    FlowPane actionPane = new FlowPane();
    actionPane.setPadding(new Insets(5));
    actionPane.setHgap(5);
    addButtons(actionPane);
    textArea = new SvgTextArea(gui);
    root.setCenter(textArea);
    root.setBottom(actionPane);
    setContent(root);
    setText("Svg");
  }

  private void addButtons(FlowPane actionPane) {
    Button viewButton = new Button("View");
    viewButton.setOnAction(a -> {
      SVGImage img = SVGLoader.load(textArea.getText());
      display(img);
    });
    actionPane.getChildren().add(viewButton);

    Button batikViewButton = new Button("View with Batik");
    batikViewButton.setOnAction(a -> {
      BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
      try (StringReader reader = new StringReader(textArea.getText())) {
        TranscoderInput input = new TranscoderInput(reader);
        transcoder.transcode(input, null);
        Image img = SwingFXUtils.toFXImage(transcoder.getBufferedImage(), null);
        display(new ImageView(img));
      } catch (TranscoderException e) {
        ExceptionAlert.showAlert("Faied to convert svg image", e);
      }
    });
    actionPane.getChildren().add(batikViewButton);

    Button loadScriptButton = new Button("Load svg file");
    actionPane.getChildren().add(loadScriptButton);
    loadScriptButton.setOnAction(a -> {
      FileChooser fc = new FileChooser();
      fc.setTitle("Select Svg file");
      fc.setInitialDirectory(new File(System.getProperty("user.dir")));
      File targetFile = fc.showOpenDialog(gui.getStage());
      if (targetFile != null) {
        try {
          textArea.setText(Files.readString(targetFile.toPath()));
          svgFile = targetFile;
          setText(targetFile.getName());
        } catch (IOException e) {
          ExceptionAlert.showAlert("Failed to read " + targetFile, e);
        }
      }
    });
    Button saveScriptButton = new Button("Save svg");
    actionPane.getChildren().add(saveScriptButton);
    saveScriptButton.setOnAction(a -> {
      if (svgFile != null) {
        try {
          Files.writeString(svgFile.toPath(), textArea.getText());
          setStatus("Saved " + svgFile);
        } catch (IOException e) {
          setStatus("Failed to write " + svgFile);
          ExceptionAlert.showAlert("Failed to write " + svgFile, e);
        }
      } else {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save svg");
        fc.setInitialDirectory(new File(System.getProperty("user.dir")));

        String template = gui.getSelectedTemplate();
        if (template != null) {
          String suggested = template.substring(0, template.lastIndexOf(".")) + ".svg";
          fc.setInitialFileName(suggested);
        }
        File targetFile = fc.showSaveDialog(gui.getStage());

        if (targetFile != null) {
          Path filePath = targetFile.toPath();
          try {
            setStatus("Writing " + filePath.toAbsolutePath());
            Files.writeString(filePath, textArea.getText());
            setStatus("Saved " + targetFile);
            setText(targetFile.getName());
            svgFile = targetFile;
          } catch (IOException e) {
            setStatus("Failed to write " + targetFile);
            ExceptionAlert.showAlert("Failed to write " + filePath, e);
          }
        }
      }
    });
  }

  private void display(Node img) {
    Stage stage = new Stage();
    BorderPane pane = new BorderPane(img);
    pane.setPadding(new Insets(10));
    Scene scene = new Scene(pane);
    stage.getIcons().add(gui.getAppIcon());
    stage.setScene(scene);
    stage.show();
  }
}
