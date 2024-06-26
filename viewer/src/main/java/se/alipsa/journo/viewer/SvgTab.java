package se.alipsa.journo.viewer;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import org.apache.batik.transcoder.TranscoderInput;
import org.girod.javafx.svgimage.SVGImage;
import org.girod.javafx.svgimage.SVGLoader;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class SvgTab extends JournoTab {
  private final SvgTextArea textArea;
  public SvgTab(JournoViewer gui) {
    super(gui, "Svg");
    textArea = new SvgTextArea(this);
    BorderPane root = new BorderPane();
    FlowPane actionPane = new FlowPane();
    actionPane.setPadding(new Insets(5));
    actionPane.setHgap(5);
    addButtons(actionPane);
    root.setCenter(textArea);
    root.setBottom(actionPane);
    setContent(root);
  }

  private void addButtons(FlowPane actionPane) {
    Button viewButton = new Button("View");
    viewButton.setTooltip(new Tooltip("view using fxsvgimage"));
    viewButton.setOnAction(a -> {
      try {
        SVGImage img = SVGLoader.load(textArea.getText());
        Popup.display(img, gui);
      } catch (Exception e) {
        ExceptionAlert.showAlert("Failed to display svg", e);
      }
    });
    actionPane.getChildren().add(viewButton);

    Button batikViewButton = new Button("View with Batik");
    batikViewButton.setOnAction(a -> {
      BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
      try (StringReader reader = new StringReader(textArea.getText())) {
        TranscoderInput input = new TranscoderInput(reader);
        transcoder.transcode(input, null);
        Image img = SwingFXUtils.toFXImage(transcoder.getBufferedImage(), null);
        Popup.display(new ImageView(img), gui);
      } catch (Exception e) {
        ExceptionAlert.showAlert("Failed to convert svg image", e);
      }
    });
    actionPane.getChildren().add(batikViewButton);

    Button loadScriptButton = new Button("Load svg file");
    actionPane.getChildren().add(loadScriptButton);
    loadScriptButton.setOnAction(a -> promptAndLoad());
    Button saveScriptButton = new Button("Save svg");
    actionPane.getChildren().add(saveScriptButton);
    saveScriptButton.setOnAction(a -> save());
    Button savePngButton = new Button("Export png");
    actionPane.getChildren().add(savePngButton);
    savePngButton.setOnAction(a -> {
      ExportPngDialog dialog = new ExportPngDialog(textArea, gui);
      Optional<File> result = dialog.showAndWait();
      result.ifPresent(file -> gui.setStatus("Saved svg as " + file));
    });
  }

  @Override
  public CodeTextArea getCodeArea() {
    return textArea;
  }

  @Override
  public void promptAndLoad() {
    FileChooser fc = new FileChooser();
    fc.setTitle("Select Svg file");
    fc.setInitialDirectory(gui.getProjectDir());
    File targetFile = fc.showOpenDialog(gui.getStage());
    if (targetFile != null) {
      try {
        textArea.setText(Files.readString(targetFile.toPath()));
        setFile(targetFile);
        setText(targetFile.getName());
      } catch (Exception e) {
        ExceptionAlert.showAlert("Failed to read " + targetFile, e);
      }
    }
  }

  @Override
  public void save() {
    if (file != null) {
      try {
        Files.writeString(file.toPath(), textArea.getText());
        setStatus("Saved " + file);
        contentSaved();
      } catch (Exception e) {
        setStatus("Failed to write " + file);
        ExceptionAlert.showAlert("Failed to write " + file, e);
      }
    } else {
      FileChooser fc = new FileChooser();
      fc.setTitle("Save svg");
      if (gui.getProjectDir() != null && gui.getProjectDir().exists()) {
        fc.setInitialDirectory(gui.getProjectDir());
      }

      if (gui.getActiveProject() != null) {
        String projectName = gui.getActiveProject().getName();
        if (projectName != null) {
          String suggested = projectName + ".svg";
          fc.setInitialFileName(suggested);
        }
      }
      File targetFile = fc.showSaveDialog(gui.getStage());

      if (targetFile != null) {
        Path filePath = targetFile.toPath();
        try {
          setStatus("Writing " + filePath.toAbsolutePath());
          Files.writeString(filePath, textArea.getText());
          setText(targetFile.getName());
          setFile(targetFile);
          setStatus("Saved " + targetFile);
        } catch (IOException e) {
          setStatus("Failed to write " + targetFile);
          ExceptionAlert.showAlert("Failed to write " + filePath, e);
        }
      }
    }
  }

  @Override
  public void clear() {
    file = null;
    setText(defaultTitle);
    textArea.clear();
  }
}
