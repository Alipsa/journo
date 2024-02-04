package se.alipsa.journo.viewer;

import freemarker.template.TemplateException;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import se.alipsa.groovy.resolver.ResolvingException;
import se.alipsa.journo.ReportEngine;

import javax.script.ScriptException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class JournoViewer extends Application {

  private PDFViewer pdfViewer;
  private GroovyTextArea codeArea;
  private ListView<String> dependencies;
  private TextField dirTf;
  private ReportEngine reportEngine;
  private Button runButton;
  private TabPane tabPane = new TabPane();
  private File groovyFile = null;
  private ComboBox<String> templateNames  = new ComboBox<>();
  private TextField statusField = new TextField();

  public static void main(String[] args) {
    launch();
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    Tab codeTab = createCodeTab();
    Tab pdfTab = createPdfTab();
    tabPane.getTabs().addAll(codeTab, pdfTab);

    Scene scene = new Scene(tabPane, 800, 900);
    scene.getStylesheets().add(getClass().getResource("/default-theme.css").toExternalForm());
    primaryStage.setResizable(true);
    primaryStage.setTitle("Journo Viewer");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private Tab createCodeTab() {
    Tab codeTab = new Tab("Data");
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
      File pdfDir = fc.showDialog(null);
      if (pdfDir != null) {
        setTemplateDir(pdfDir);
      }
    });

    templateNames.setOnAction(a -> setTemplate(templateNames.getSelectionModel().getSelectedItem()));
    HBox.setHgrow(dirTf, Priority.ALWAYS);
    buttonPane.getChildren().addAll(label, dirTf, browseButton, templateNames);
    root.setTop(buttonPane);

    VBox codeBox = new VBox();
    Label codeLabel = new Label("Groovy Code to generate data (must return a Map<String, Object>)");
    codeLabel.setPadding(new Insets(5));
    codeArea = new GroovyTextArea(this);
    codeArea.setPadding(new Insets(5));
    VBox.setVgrow(codeArea, Priority.ALWAYS);
    codeBox.getChildren().addAll(codeLabel, codeArea);
    root.setCenter(codeBox);

    VBox dependenciesBox = new VBox();
    Label depLabel = new Label("Dependencies");
    depLabel.setPadding(new Insets(5));
    dependencies = new ListView<>();
    dependencies.setContextMenu(createOutsideContextMenu(dependencies));
    dependencies.setCellFactory(lv -> createListCell());
    dependencies.getItems().addListener((ListChangeListener<? super String>) c -> {
      try {
        codeArea.setDependencies(dependencies.getItems());
      } catch (ResolvingException e) {
        ExceptionAlert.showAlert("Failed to add dependency", e);
      }
    });
    VBox.setVgrow(dependencies, Priority.ALWAYS);
    dependenciesBox.getChildren().addAll(depLabel, dependencies);
    root.setRight(dependenciesBox);

    HBox actionPane = new HBox();
    actionPane.setPadding(new Insets(5));
    actionPane.setSpacing(5);
    Button loadScriptButton = new Button("Load groovy script");
    loadScriptButton.setOnAction(a -> {
      FileChooser fc = new FileChooser();
      fc.setTitle("Select Groovy Script");
      fc.setInitialDirectory(new File(System.getProperty("user.dir")));
      groovyFile = fc.showOpenDialog(null);
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
          statusField.setText("Saved " + groovyFile);
        } catch (IOException e) {
          statusField.setText("Failed to write " + groovyFile);
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
        File targetFile = fc.showSaveDialog(null);

        if (targetFile != null) {
          Path filePath = targetFile.toPath();
          try {
            statusField.setText("Writing " + filePath.toAbsolutePath());
            Files.writeString(filePath, codeArea.getText());
            statusField.setText("Saved " + groovyFile);
          } catch (IOException e) {
            statusField.setText("Failed to write " + groovyFile);
            ExceptionAlert.showAlert("Failed to write " + filePath, e);
          }
        }
      }
    });
    runButton = new Button("Run");
    runButton.setDisable(true);
    runButton.setOnAction(a -> run());
    HBox.setHgrow(statusField, Priority.ALWAYS);
    actionPane.getChildren().addAll(loadScriptButton,saveScriptButton, runButton, statusField);
    root.setBottom(actionPane);

    codeTab.setContent(root);
    return codeTab;
  }

  private ListCell<String> createListCell() {
    ListCell<String> cell = new ListCell<>();
    ContextMenu contextMenu = new ContextMenu();
    MenuItem deleteItem = new MenuItem();
    deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", cell.itemProperty()));
    deleteItem.setOnAction(event -> dependencies.getItems().remove(cell.getItem()));
    contextMenu.getItems().addAll(deleteItem);
    cell.textProperty().bind(cell.itemProperty());
    cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
      if (isNowEmpty) {
        cell.setContextMenu(null);
      } else {
        cell.setContextMenu(contextMenu);
      }
    });
    return cell;
  }

  private static ContextMenu createOutsideContextMenu(ListView<String> dependencies) {
    ContextMenu outsideContextMenu = new ContextMenu();
    MenuItem addDependencyMI = new MenuItem("add");
    addDependencyMI.setOnAction(a -> {
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("Add dependency");
      dialog.setHeaderText("Add a maven dependency in the form groupId:artifactId:version");
      dialog.setContentText("Dependency");
      Optional<String> result = dialog.showAndWait();
      result.ifPresent(d ->  {
        System.out.println("adding " + d);
        dependencies.getItems().add(d);
      });
    });
    outsideContextMenu.getItems().add(addDependencyMI);
    return outsideContextMenu;
  }

  private void setTemplateDir(File pdfDir) {
    dirTf.setText(pdfDir.getAbsolutePath());
    templateNames.getItems().clear();
    FilenameFilter filter = (dir, name) -> name.endsWith(".ftl") || name.endsWith(".ftlh");
    String[] templateFileNames = pdfDir.list(filter);
    if (templateFileNames != null) {
      templateNames.getItems().addAll(templateFileNames);
    }
    System.setProperty("user.dir", pdfDir.getAbsolutePath());
    try {
      reportEngine = new ReportEngine(pdfDir);
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to initialize the Journo ReportEngine", e);
    }
  }

  private void setTemplate(String selectedItem) {
    runButton.setDisable(false);
  }

  private Tab createPdfTab() throws IOException {
    Tab pdfTab = new Tab("PDF output");
    BorderPane root = new BorderPane();

    FlowPane buttonPane = new FlowPane();
    buttonPane.setPadding(new Insets(5));
    buttonPane.setAlignment(Pos.CENTER);
    Button reloadButton = new Button("Reload");
    buttonPane.getChildren().add(reloadButton);
    reloadButton.setOnAction(a -> run());
    root.setTop(buttonPane);

    pdfViewer = new PDFViewer();
    root.setCenter(pdfViewer);

    pdfTab.setContent(root);
    return pdfTab;
  }

  private void run() {
    try {
      Map<String, Object> data = codeArea.executeGroovyScript();
      byte[] pdf = reportEngine.renderPdf(templateNames.getSelectionModel().getSelectedItem(), data);
      pdfViewer.load(pdf);
      tabPane.getSelectionModel().select(1);
    } catch (ResolvingException e) {
      ExceptionAlert.showAlert("Failed to add dependencies to groovy classpath", e);
    } catch (ScriptException e) {
      ExceptionAlert.showAlert("Failed to execute groovy script", e);
    } catch (IOException | TemplateException e) {
      ExceptionAlert.showAlert("Failed to render the pdf", e);
    }
  }

  public void setStatus(String status) {
    statusField.setText(status);
  }

}
