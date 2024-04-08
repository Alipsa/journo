package se.alipsa.journo.viewer;

import static se.alipsa.journo.viewer.JournoViewer.*;

import freemarker.template.TemplateException;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import se.alipsa.journo.ReportEngine;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FreemarkerTab extends JournoTab {

  private ReportEngine reportEngine;
  private Button templateRunButton;
  private TextField dirTf;
  private FreemarkerTextArea freeMarkerArea;
  private final ComboBox<String> templateNames  = new ComboBox<>();
  private File markupFile = null;

  public FreemarkerTab(JournoViewer gui) {
    super(gui);
    setText("Template");
    setClosable(false);
    BorderPane root = new BorderPane();
    HBox buttonPane = new HBox();
    buttonPane.setPadding(new Insets(5));
    buttonPane.setSpacing(5);
    Label label = new Label("Dir");
    dirTf = new TextField();
    dirTf.setDisable(true);
    Button browseButton = new Button("...");
    browseButton.setOnAction(a -> {
      DirectoryChooser fc = new DirectoryChooser();
      fc.setTitle("Select dir where Freemarker templates are located");
      File dir = new File(gui.getPref(TEMPLATE_DIR, "."));
      if (dir.isFile()){
        dir = dir.getParentFile();
      }
      if (!dir.exists()) {
        dir = new File(".");
      }
      fc.setInitialDirectory(dir);
      File pdfDir = fc.showDialog(gui.getStage());
      if (pdfDir != null) {
        setTemplateDir(pdfDir.toPath());
      }
    });

    templateNames.setOnAction(a -> {
          String selectedItem = templateNames.getSelectionModel().getSelectedItem();
          if (selectedItem != null)
            setTemplate(templateNames.getSelectionModel().getSelectedItem());
        }
    );
    HBox.setHgrow(dirTf, Priority.ALWAYS);
    buttonPane.getChildren().addAll(label, dirTf, browseButton, templateNames);
    root.setTop(buttonPane);

    VBox codeBox = new VBox();
    freeMarkerArea = new FreemarkerTextArea(this);
    freeMarkerArea.setPadding(new Insets(5));
    VBox.setVgrow(freeMarkerArea, Priority.ALWAYS);
    codeBox.getChildren().addAll(freeMarkerArea);
    root.setCenter(codeBox);
    HBox actionPane = new HBox();
    actionPane.setPadding(new Insets(5));
    actionPane.setSpacing(5);
    Button loadTemplateButton = createLoadTemplateButton();
    Button saveTemplateButton = createSaveTemplateButton();
    templateRunButton = new Button("View PDF");
    templateRunButton.setOnAction(a -> gui.run());
    actionPane.getChildren().addAll(loadTemplateButton, saveTemplateButton, templateRunButton);
    root.setBottom(actionPane);

    setContent(root);
  }

  private Button createLoadTemplateButton() {
    Button loadScriptButton = new Button("Load template");
    loadScriptButton.setOnAction(a -> {
      FileChooser fc = new FileChooser();
      fc.setTitle("Select Freemarker template");
      fc.setInitialDirectory(gui.getProjectDir());
      File targetFile = fc.showOpenDialog(gui.getStage());
      if (targetFile != null) {
        try {
          freeMarkerArea.setText(Files.readString(targetFile.toPath()));
          markupFile = targetFile;
          setText(targetFile.getName());
          gui.setProjectTemplateFile(targetFile.toPath());
          enableRunButton();
        } catch (IOException e) {
          ExceptionAlert.showAlert("Failed to load Freemarker template", e);
        }
      }
    });
    return loadScriptButton;
  }

  private Button createSaveTemplateButton() {
    Button saveTemplateButton = new Button("Save template");
    saveTemplateButton.setOnAction(a -> {
      if (markupFile != null) {
        try {
          Files.writeString(markupFile.toPath(), freeMarkerArea.getText());
          setStatus("Saved " + markupFile);
          gui.saveTemplateFileToProject(markupFile);
          contentSaved();
        } catch (IOException e) {
          setStatus("Failed to write " + markupFile);
          ExceptionAlert.showAlert("Failed to write " + markupFile, e);
        }
      } else {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save template");
        fc.setInitialDirectory(gui.getProjectDir());
        String template = templateNames.getSelectionModel().getSelectedItem();
        if (template != null) {
          String suggested = template.substring(0, template.lastIndexOf(".")) + ".ftl";
          fc.setInitialFileName(suggested);
        }
        File targetFile = fc.showSaveDialog(gui.getStage());

        if (targetFile != null) {
          Path filePath = targetFile.toPath();
          try {
            setStatus("Writing " + filePath.toAbsolutePath());
            Files.writeString(filePath, freeMarkerArea.getText());
            setStatus("Saved " + markupFile);
            contentSaved();
            gui.saveTemplateFileToProject(markupFile);
          } catch (IOException e) {
            setStatus("Failed to write " + markupFile);
            ExceptionAlert.showAlert("Failed to write " + filePath, e);
          }
        }
      }
    });
    return saveTemplateButton;
  }

  public void disbleRunButton() {
    templateRunButton.setDisable(true);
  }

  public void enableRunButton() {
    templateRunButton.setDisable(false);
  }

  private void setTemplateDir(Path pdfPath) {
    File pdfDir = pdfPath.toFile();
    dirTf.setText(pdfDir.getAbsolutePath());
    gui.disableRunButtons();
    templateNames.getItems().clear();
    markupFile = null;
    FilenameFilter filter = (dir, name) -> name.endsWith(".ftl") || name.endsWith(".ftlh");
    String[] templateFileNames = pdfDir.list(filter);
    if (templateFileNames != null) {
      templateNames.getItems().addAll(templateFileNames);
    }
    gui.setPref(TEMPLATE_DIR, pdfDir.getAbsolutePath());
    try {
      reportEngine = new ReportEngine(pdfDir);
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to initialize the Journo ReportEngine", e);
    }
  }

  private void setTemplate(String selectedItem) {
    gui.enableRunButtons();
    //System.out.println("dirTf = " + dirTf + ", dirTf.getText() = " + (dirTf == null ? "null" : dirTf.getText()) + ", selectedItem = " + selectedItem);
    File templateFile = new File(dirTf.getText(), selectedItem);
    gui.setProjectTemplateFile(templateFile.toPath());
    try {
      String content = Files.readString(templateFile.toPath());
      templateNames.setValue(selectedItem);
      freeMarkerArea.setText(content);
      this.markupFile = templateFile;
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to read " + templateFile, e);
    }
  }

  public String getSelectedTemplate() {
    return templateNames.getSelectionModel().getSelectedItem();
  }

  public Path getTemplateFile() {
    if (getSelectedTemplate() == null) {
      return null;
    }
    File file = new File(dirTf.getText(), getSelectedTemplate());
    return file.exists() ? file.toPath() : null;
  }
  public byte[] renderPdf(Map<String, Object> data) throws TemplateException, IOException {
    return reportEngine.renderPdf(getSelectedTemplate(), data);
  }

  public void loadFile(Path templateFile) {
    if (templateFile == null) {
      return;
    }
    setTemplateDir(templateFile.getParent());
    setTemplate(templateFile.getFileName().toString());
  }

  @Override
  public CodeTextArea getCodeArea() {
    return freeMarkerArea;
  }
}
