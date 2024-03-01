package se.alipsa.journo.viewer;

import freemarker.template.TemplateException;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
//import se.alipsa.groovy.resolver.ResolvingException;
import org.apache.commons.io.FileUtils;
import se.alipsa.journo.ReportEngine;

import javax.script.ScriptException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.prefs.Preferences;

public class JournoViewer extends Application {

  private static final String TEMPLATE_DIR = JournoViewer.class.getName() + ".templateDir";
  private PDFViewer pdfViewer;
  private GroovyTextArea codeArea;

  private FreemarkerTextArea freeMarkerArea;
  private ListView<File> jarDependencies;
  private TextField dirTf;
  private ReportEngine reportEngine;
  private Button templateRunButton;
  private Button codeRunButton;
  private final TabPane tabPane = new TabPane();
  private File groovyFile = null;
  private File markupFile = null;
  private final ComboBox<String> templateNames  = new ComboBox<>();
  private final TextField statusField = new TextField();
  private Stage stage;

  public static void main(String[] args) {
    launch();
  }

  @Override
  public void start(Stage primaryStage) {
    this.stage = primaryStage;
    Tab freeMarkerTab = createFreeMarkerTab();
    Tab codeTab = createCodeTab();
    Tab pdfTab = createPdfTab();
    tabPane.getTabs().addAll(freeMarkerTab, codeTab, pdfTab);
    BorderPane root = new BorderPane();
    root.setCenter(tabPane);
    statusField.setDisable(true);
    root.setBottom(statusField);

    disableRunButtons();
    Scene scene = new Scene(root, 950, 960);
    scene.getStylesheets().add(getClass().getResource("/default-theme.css").toExternalForm());
    primaryStage.getIcons().add(new Image(JournoViewer.class.getResourceAsStream("/journo-logo.png")));
    primaryStage.setResizable(true);
    primaryStage.setTitle("Journo Viewer");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private void disableRunButtons() {
    templateRunButton.setDisable(true);
    codeRunButton.setDisable(true);
  }

  private void enableRunButtons() {
    templateRunButton.setDisable(false);
    codeRunButton.setDisable(false);
  }

  private Tab createFreeMarkerTab() {
    Tab templateTab = new Tab("Template");
    templateTab.setClosable(false);
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
      File dir = new File(getPref(TEMPLATE_DIR, "."));
      if (dir.isFile()){
        dir = dir.getParentFile();
      }
      if (!dir.exists()) {
        dir = new File(".");
      }
      fc.setInitialDirectory(dir);
      File pdfDir = fc.showDialog(stage);
      if (pdfDir != null) {
        setTemplateDir(pdfDir);
      }
    });

    templateNames.setOnAction(a -> setTemplate(templateNames.getSelectionModel().getSelectedItem()));
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
    Button saveTemplateButton = createSaveTemplateButton();
    templateRunButton = new Button("Run");
    templateRunButton.setOnAction(a -> run());
    actionPane.getChildren().addAll(saveTemplateButton, templateRunButton);
    root.setBottom(actionPane);

    templateTab.setContent(root);
    return templateTab;
  }

  private Button createSaveTemplateButton() {
    Button saveTemplateButton = new Button("Save template");
    saveTemplateButton.setOnAction(a -> {
      if (markupFile != null) {
        try {
          Files.writeString(markupFile.toPath(), freeMarkerArea.getText());
          setStatus("Saved " + markupFile);
        } catch (IOException e) {
          setStatus("Failed to write " + markupFile);
          ExceptionAlert.showAlert("Failed to write " + markupFile, e);
        }
      } else {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save template");
        fc.setInitialDirectory(new File(System.getProperty("user.dir")));
        String template = templateNames.getSelectionModel().getSelectedItem();
        if (template != null) {
          String suggested = template.substring(0, template.lastIndexOf(".")) + ".ftl";
          fc.setInitialFileName(suggested);
        }
        File targetFile = fc.showSaveDialog(stage);

        if (targetFile != null) {
          Path filePath = targetFile.toPath();
          try {
            setStatus("Writing " + filePath.toAbsolutePath());
            Files.writeString(filePath, freeMarkerArea.getText());
            setStatus("Saved " + markupFile);
          } catch (IOException e) {
            setStatus("Failed to write " + markupFile);
            ExceptionAlert.showAlert("Failed to write " + filePath, e);
          }
        }
      }
    });
    return saveTemplateButton;
  }

  private Preferences preferences() {
    return Preferences.userRoot().node(this.getClass().getName());
  }
  private String getPref(String preference, String fallback) {
    return preferences().get(preference, fallback);
  }

  private void setPref(String preference, String value) {
    preferences().put(preference, value);
  }


  private Tab createCodeTab() {
    Tab codeTab = new Tab("Data");
    codeTab.setClosable(false);
    BorderPane root = new BorderPane();

    VBox codeBox = new VBox();
    Label codeLabel = new Label("Groovy Code to generate data (must return a Map<String, Object>)");
    codeLabel.setPadding(new Insets(5));
    codeArea = new GroovyTextArea(this);
    codeArea.setPadding(new Insets(5));
    VBox.setVgrow(codeArea, Priority.ALWAYS);
    codeBox.getChildren().addAll(codeLabel, codeArea);
    root.setCenter(codeBox);

    VBox dependenciesBox = new VBox();
    Label depLabel = new Label("Jar dependencies");
    depLabel.setPadding(new Insets(5));
    jarDependencies = new ListView<>();
    jarDependencies.setContextMenu(createOutsideContextMenu(jarDependencies));
    jarDependencies.setCellFactory(lv -> new FileCell());
    jarDependencies.getItems().addListener((ListChangeListener<? super File>) c -> {
        codeArea.setDependencies(jarDependencies.getItems());
    });
    VBox.setVgrow(jarDependencies, Priority.ALWAYS);
    dependenciesBox.getChildren().addAll(depLabel, jarDependencies);
    root.setRight(dependenciesBox);

    HBox actionPane = new HBox();
    actionPane.setPadding(new Insets(5));
    actionPane.setSpacing(5);
    Button loadScriptButton = new Button("Load groovy script");
    loadScriptButton.setOnAction(a -> {
      FileChooser fc = new FileChooser();
      fc.setTitle("Select Groovy Script");
      fc.setInitialDirectory(new File(System.getProperty("user.dir")));
      groovyFile = fc.showOpenDialog(stage);
      if (groovyFile != null) {
        try {
          codeArea.setText(Files.readString(groovyFile.toPath()));
        } catch (IOException e) {
          ExceptionAlert.showAlert("Failed to read " + groovyFile, e);
        }
      }
    });
    Button saveScriptButton = new Button("Save groovy script");
    saveScriptButton.setOnAction(a -> {
      if (groovyFile != null) {
        try {
          Files.writeString(groovyFile.toPath(), codeArea.getText());
          setStatus("Saved " + groovyFile);
        } catch (IOException e) {
          setStatus("Failed to write " + groovyFile);
          ExceptionAlert.showAlert("Failed to write " + groovyFile, e);
        }
      } else {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save groovy script");
        fc.setInitialDirectory(new File(System.getProperty("user.dir")));
        String template = templateNames.getSelectionModel().getSelectedItem();
        if (template != null) {
          String suggested = template.substring(0, template.lastIndexOf(".")) + ".groovy";
          fc.setInitialFileName(suggested);
        }
        File targetFile = fc.showSaveDialog(stage);

        if (targetFile != null) {
          Path filePath = targetFile.toPath();
          try {
            setStatus("Writing " + filePath.toAbsolutePath());
            Files.writeString(filePath, codeArea.getText());
            setStatus("Saved " + groovyFile);
          } catch (IOException e) {
            setStatus("Failed to write " + groovyFile);
            ExceptionAlert.showAlert("Failed to write " + filePath, e);
          }
        }
      }
    });
    codeRunButton = new Button("Run");
    codeRunButton.setOnAction(a -> run());
    actionPane.getChildren().addAll(loadScriptButton,saveScriptButton, codeRunButton);
    root.setBottom(actionPane);

    codeTab.setContent(root);
    return codeTab;
  }


  private class FileCell extends ListCell<File> {

    public FileCell() {
      ContextMenu contextMenu = new ContextMenu();
      MenuItem deleteItem = new MenuItem();
      deleteItem.textProperty().bind(Bindings.format("Remove \"%s\"", itemProperty()));
      deleteItem.setOnAction(event -> jarDependencies.getItems().remove(getItem()));
      contextMenu.getItems().addAll(deleteItem);
      emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
        if (isNowEmpty) {
          setContextMenu(null);
        } else {
          setContextMenu(contextMenu);
        }
      });
    }
    @Override
    protected void updateItem(File item, boolean empty) {
      super.updateItem(item, empty);
      setText(item == null ? "" : item.getName());
    }
  }

  private ContextMenu createOutsideContextMenu(ListView<File> dependencies) {
    ContextMenu outsideContextMenu = new ContextMenu();
    MenuItem addDependencyMI = new MenuItem("add");
    addDependencyMI.setOnAction(a -> {
      FileChooser fc = new FileChooser();
      fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Jar file", "*.jar"));
      fc.setTitle("Add jar dependency");
      File jarFile = fc.showOpenDialog(stage);
      if (jarFile != null) {
        System.out.println("adding " + jarFile);
        dependencies.getItems().add(jarFile);
      }
    });
    outsideContextMenu.getItems().add(addDependencyMI);
    return outsideContextMenu;
  }


  private void setTemplateDir(File pdfDir) {
    dirTf.setText(pdfDir.getAbsolutePath());
    disableRunButtons();
    templateNames.getItems().clear();
    markupFile = null;
    FilenameFilter filter = (dir, name) -> name.endsWith(".ftl") || name.endsWith(".ftlh");
    String[] templateFileNames = pdfDir.list(filter);
    if (templateFileNames != null) {
      templateNames.getItems().addAll(templateFileNames);
    }
    System.setProperty("user.dir", pdfDir.getAbsolutePath());
    setPref(TEMPLATE_DIR, pdfDir.getAbsolutePath());
    try {
      reportEngine = new ReportEngine(pdfDir);
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to initialize the Journo ReportEngine", e);
    }
  }

  private void setTemplate(String selectedItem) {
    enableRunButtons();
    File templateFile = new File(dirTf.getText(), selectedItem);
    try {
      String content = Files.readString(templateFile.toPath());
      freeMarkerArea.replaceText(content);
      this.markupFile = templateFile;
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to read " + templateFile, e);
    }
  }

  private Tab createPdfTab() {
    Tab pdfTab = new Tab("PDF output");
    pdfTab.setClosable(false);
    BorderPane root = new BorderPane();

    FlowPane buttonPane = new FlowPane();
    buttonPane.setPadding(new Insets(5));
    buttonPane.setAlignment(Pos.CENTER);
    Button reloadButton = new Button("Reload");
    buttonPane.getChildren().add(reloadButton);
    reloadButton.setOnAction(a -> run());
    Button saveButton = new Button("Save");
    saveButton.setOnAction(a -> {
      FileChooser fc = new FileChooser();
      File file = fc.showSaveDialog(stage);
      if (file != null) {
        try {
          writeToFile(file, pdfViewer.getContent());
        } catch (IOException e) {
          ExceptionAlert.showAlert("Failed to save " + file, e);
        }
      }
    });
    buttonPane.getChildren().add(saveButton);

    root.setTop(buttonPane);

    pdfViewer = new PDFViewer();
    root.setCenter(pdfViewer);

    pdfTab.setContent(root);
    return pdfTab;
  }

  private void run() {
    try {
      Map<String, Object> data = codeArea.executeGroovyScript();
      // TODO: Check if templateArea is saved and if not save if loaded from file or prompt to save to new file
      byte[] pdf = reportEngine.renderPdf(templateNames.getSelectionModel().getSelectedItem(), data);
      pdfViewer.load(pdf);
      tabPane.getSelectionModel().select(2);
    } catch (ScriptException | RuntimeException e) {
      ExceptionAlert.showAlert("Failed to execute groovy script", e);
    } catch (IOException | TemplateException e) {
      ExceptionAlert.showAlert("Failed to render the pdf", e);
    }
  }

  public void setStatus(String status) {
    statusField.setText(status);
  }

  public static Path writeToFile(File file, byte[] content) throws IOException {
    return Files.write(file.toPath(), content);
  }

}
