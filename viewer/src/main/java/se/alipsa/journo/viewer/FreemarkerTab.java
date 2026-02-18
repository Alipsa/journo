package se.alipsa.journo.viewer;

import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import se.alipsa.journo.JournoException;
import se.alipsa.journo.JournoEngine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FreemarkerTab extends JournoTab {

  private JournoEngine journoEngine;
  private FreemarkerTextArea freeMarkerArea;

  public FreemarkerTab(JournoViewer gui) {
    super(gui, "Template");
    setClosable(false);
    BorderPane root = new BorderPane();

    VBox codeBox = new VBox();
    freeMarkerArea = new FreemarkerTextArea(this);
    freeMarkerArea.setPadding(new Insets(5));
    VBox.setVgrow(freeMarkerArea, Priority.ALWAYS);
    codeBox.getChildren().addAll(freeMarkerArea);
    root.setCenter(codeBox);
    setContent(root);
  }

  public void promptAndLoad() {
    FileChooser fc = new FileChooser();
    fc.setTitle("Select Freemarker template");
    fc.setInitialDirectory(gui.getProjectDir());
    File targetFile = fc.showOpenDialog(gui.getStage());
    if (targetFile != null) {
      try {
        freeMarkerArea.setText(Files.readString(targetFile.toPath()));
        file = targetFile;
        setText(targetFile.getName());
        gui.setProjectTemplateFile(targetFile.toPath());
        gui.enableRunButton();
      } catch (IOException e) {
        ExceptionAlert.showAlert("Failed to load Freemarker template", e);
      }
    }
  }

  public byte[] renderPdf(Map<String, Object> data) throws JournoException {
    return journoEngine.renderPdf(file.getName(), data);
  }

  public void renderPdf(Map<String, Object> data, File toFile) throws JournoException, IOException {
    byte[] content = renderPdf(data);
    FileUtils.writeByteArrayToFile(toFile, content);
  }

  public String renderHtml(Map<String, Object> data) throws JournoException {
    return journoEngine.renderHtml(file.getName(), data);
  }

  public void loadFile(Path templateFile) {
    if (templateFile == null) {
      return;
    }
    try {
      freeMarkerArea.setText(Files.readString(templateFile));
      setTitle(templateFile.getFileName().toString());
      gui.enableRunButton();
      setFile(templateFile.toFile());
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to load " + templateFile, e);
    }
  }

  @Override
  public CodeTextArea getCodeArea() {
    return freeMarkerArea;
  }

  @Override
  public void save() {
    if (file != null) {
      try {
        Files.writeString(file.toPath(), freeMarkerArea.getText());
        setStatus("Saved " + file);
        gui.saveTemplateFileToProject(file);
        contentSaved();
      } catch (IOException e) {
        setStatus("Failed to write " + file);
        ExceptionAlert.showAlert("Failed to write " + file, e);
      }
    } else {
      FileChooser fc = new FileChooser();
      fc.setTitle("Save template");
      fc.setInitialDirectory(gui.getProjectDir());

      String template = gui.getActiveProject().getName();
      if (template != null) {
        String suggested = template + ".ftl";
        fc.setInitialFileName(suggested);
      }
      File targetFile = fc.showSaveDialog(gui.getStage());

      if (targetFile != null) {
        Path filePath = targetFile.toPath();
        try {
          setStatus("Writing " + filePath.toAbsolutePath());
          Files.writeString(filePath, freeMarkerArea.getText());
          setFile(targetFile);
          contentSaved();
          gui.saveTemplateFileToProject(file);
          setStatus("Saved " + file);
        } catch (IOException e) {
          setStatus("Failed to write " + file);
          ExceptionAlert.showAlert("Failed to write " + filePath, e);
        }
      }
    }
  }

  @Override
  public void setFile(File file) {
    if (super.file != null && file != null && !super.file.getParentFile().equals(file.getParentFile())) {
      journoEngine = null;
    }
    if (journoEngine == null && file != null) {
      try {
        journoEngine = new JournoEngine(file.getParentFile());
      } catch (IOException e) {
        ExceptionAlert.showAlert("Failed to re-initialize the reportEngine", e);
      }
    }
    super.setFile(file);
  }

  @Override
  public void clear() {
    file = null;
    setText(defaultTitle);
    freeMarkerArea.clear();
  }
}
