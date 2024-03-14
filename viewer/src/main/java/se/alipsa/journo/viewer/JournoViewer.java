package se.alipsa.journo.viewer;

import freemarker.template.TemplateException;
import groovy.lang.GroovySystem;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class JournoViewer extends Application {

  public static final String TEMPLATE_DIR = JournoViewer.class.getName() + ".templateDir";
  private PDFViewer pdfViewer;
  FreemarkerTab freeMarkerTab;
  Tab pdfTab;
  GroovyTab codeTab;
  private final TabPane tabPane = new TabPane();

  private final TextField statusField = new TextField();
  private Stage stage;
  private Scene scene;
  private final ComboBox<Project> projectCombo = new ComboBox<>();
  Image appIcon;

  public static void main(String[] args) {
    launch();
  }

  @Override
  public void start(Stage primaryStage) {
    this.stage = primaryStage;
    freeMarkerTab = new FreemarkerTab(this);
    codeTab = new GroovyTab(this);
    pdfTab = createPdfTab();
    tabPane.getTabs().addAll(freeMarkerTab, codeTab, pdfTab);
    BorderPane root = new BorderPane();
    root.setCenter(tabPane);
    statusField.setDisable(true);
    root.setBottom(statusField);
    HBox topBox = new HBox();
    MenuBar menuBar = createMenu();
    topBox.getChildren().add(menuBar);
    topBox.getChildren().add(createProjectBar());
    HBox.setHgrow(menuBar, Priority.ALWAYS);
    root.setTop(topBox);

    disableRunButtons();
    scene = new Scene(root, 950, 980);
    scene.getStylesheets().add(getClass().getResource("/default-theme.css").toExternalForm());
    appIcon = new Image(JournoViewer.class.getResourceAsStream("/journo-logo.png"));
    primaryStage.getIcons().add(appIcon);
    primaryStage.setResizable(true);
    primaryStage.setTitle("Journo Viewer");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private Node createProjectBar() {
    HBox hbox = new HBox();
    hbox.setSpacing(5);
    hbox.setPadding(new Insets(1));
    hbox.setStyle("-fx-background-color: -fx-body-color; -fx-border-color: lightgray");
    Label label = new Label("Project");
    label.setPadding(new Insets(4, 0, 0, 5));
    label.setAlignment(Pos.BOTTOM_CENTER);

    hbox.getChildren().add(label);
    hbox.getChildren().add(projectCombo);
    try {
      populateProjectCombo(projectCombo);
    } catch (Exception e) {
      ExceptionAlert.showAlert("Failed to load project from preferences", e);
    }
    projectCombo.setOnAction(a -> setActiveProject(projectCombo.getValue()));
    Button loadButton = new Button("load");
    hbox.getChildren().add(loadButton);
    loadButton.setOnAction(a -> {
      FileChooser fc = new FileChooser();
      fc.setInitialDirectory(new File(System.getProperty("user.dir")));
      fc.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Journo project files", ".jpr"));
      File projectFile = fc.showOpenDialog(getStage());
      if (projectFile != null) {
        try {
          Project p = Project.load(projectFile.getAbsolutePath());
          projectCombo.getItems().add(p);
          projectCombo.setValue(p);
          setActiveProject(p);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    });
    Button saveButton = new Button("save");
    hbox.getChildren().add(saveButton);
    saveButton.setOnAction(a -> {
      if (projectCombo.getValue() != null) {
        try {
          saveProject(projectCombo.getValue());
        } catch (IOException e) {
          ExceptionAlert.showAlert("Failed to save project", e);
        }
      }
    });

    Button newProjectButton = new Button("new");
    hbox.getChildren().add(newProjectButton);
    newProjectButton.setOnAction(a -> {
      TextInputDialog tid = new TextInputDialog();
      tid.setTitle("Create new Project");
      tid.setHeaderText("Name of project");
      tid.setContentText("name");
      Optional<String> response = tid.showAndWait();
      if (response.isEmpty()) {
        return;
      }
      Project p = new Project();
      p.setName(response.get());
      p.setTemplateFile(freeMarkerTab.getTemplateFile());
      p.setDataFile(codeTab.getScriptFile());
      projectCombo.getItems().add(p);
      projectCombo.setValue(p);
      setActiveProject(p);
    });

    return hbox;
  }

  private void setActiveProject(Project p) {
    freeMarkerTab.loadFile(p.getTemplateFile());
    codeTab.loadFile(p.getDataFile());
    codeTab.setDependencies(p.getDependencyList());
  }

  private MenuBar createMenu() {
    MenuBar menuBar = new MenuBar();
    Menu graphicsMenu = new Menu("Graphics");
    menuBar.getMenus().add(graphicsMenu);
    MenuItem addSvgTabMi = new MenuItem("Add SVG tab");
    graphicsMenu.getItems().add(addSvgTabMi);
    addSvgTabMi.setOnAction(a -> {
      SvgTab svgTab = new SvgTab(this);
      tabPane.getTabs().add(svgTab);
      tabPane.getSelectionModel().select(svgTab);
    });
    Menu helpMenu = new Menu("Help");
    menuBar.getMenus().add(helpMenu);
    MenuItem about = new MenuItem("about");
    helpMenu.getItems().add(about);
    about.setOnAction(a -> {
      StringBuilder content = new StringBuilder();
      String version = "unknown";
      try (InputStream is = getClass().getResourceAsStream("/META-INF/MANIFEST.MF")) {
        if (is != null) {
          BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
          String line;
          while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("Implementation-Version")) {
              version = line.substring(line.indexOf(':'));
            }
          }
        } else {
          content.append("Failed to find MANIFEST.MF");
        }

      } catch (IOException e) {
        ExceptionAlert.showAlert("Error reading manifest", e);
      }
      content.append("Journo version: ").append(version)
      .append("\nJava Runtime Version: ")
      .append(System.getProperty("java.runtime.version"))
          .append(" (").append(System.getProperty("os.arch")).append(")")
          .append(")")
          .append("\nGroovy version: ").append(GroovySystem.getVersion());
      Alert infoDialog = new Alert(Alert.AlertType.INFORMATION, content.toString());
      infoDialog.setHeaderText("About Journo");
      infoDialog.show();
    });
    return menuBar;
  }

  void populateProjectCombo(ComboBox<Project> projectCombo) throws BackingStoreException, IOException {
    Preferences projects = preferences().node("projects");
    ObservableList<Project> list = projectCombo.getItems();
    for (String name : projects.childrenNames()) {
      String path = projects.node(name).get("projectFile", null);
      list.add(Project.load(path));
    }
  }

  void saveProject(Project p) throws IOException {
    String projectFilePath = preferences().node(p.getName()).get("projectFile", null);
    if (projectFilePath == null) {
      FileChooser fc = new FileChooser();
      fc.setTitle("Save Journo project file");
      fc.setInitialDirectory(new File(System.getProperty("user.dir")));
      fc.setInitialFileName(p.getName() + ".jpr");
      File file = fc.showSaveDialog(getStage());
      if (file == null) {
        return;
      }
      projectFilePath = file.getAbsolutePath();
    }
    saveProject(p, new File(projectFilePath));
  }

  void saveProject(Project p, File path) throws IOException {
    String projectFilePath = path.getAbsolutePath();
    System.out.println("Saving project: " + p.values());
    Project.save(p, projectFilePath);
    Preferences projects = preferences().node("projects");
    projects.node(p.getName()).put("projectFile", projectFilePath);
  }


  void disableRunButtons() {
    freeMarkerTab.disbleRunButton();
    codeTab.disbleRunButton();

  }

  void enableRunButtons() {
    freeMarkerTab.enableRunButton();
    codeTab.enableRunButton();
  }



  private Preferences preferences() {
    return Preferences.userRoot().node(this.getClass().getName());
  }
  String getPref(String preference, String fallback) {
    return preferences().get(preference, fallback);
  }

  void setPref(String preference, String value) {
    preferences().put(preference, value);
  }

  public Stage getStage() {
    return stage;
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

  void run() {
    scene.setCursor(Cursor.WAIT);
    try {
      Map<String, Object> data = codeTab.runScript();
      // TODO: Check if templateArea is saved and if not save if loaded from file or prompt to save to new file
      byte[] pdf = freeMarkerTab.renderPdf(data);
      pdfViewer.load(pdf);
      tabPane.getSelectionModel().select(pdfTab);
      scene.setCursor(Cursor.DEFAULT);
    } catch (IOException | TemplateException e) {
      scene.setCursor(Cursor.DEFAULT);
      ExceptionAlert.showAlert("Failed to render the pdf", e);
    }  catch (Throwable e) {
      scene.setCursor(Cursor.DEFAULT);
      ExceptionAlert.showAlert("Failed to execute groovy script", e);
    }
  }

  public void setStatus(String status) {
    statusField.setText(status);
  }

  public static Path writeToFile(File file, byte[] content) throws IOException {
    return Files.write(file.toPath(), content);
  }

  public String getSelectedTemplate() {
    return freeMarkerTab.getSelectedTemplate();
  }

  public Image getAppIcon() {
    return appIcon;
  }

  public void setProjectTemplateFile(File templateFile) {
    Project p = projectCombo.getValue();
    if (p == null) {
      return;
    }
    System.out.println("Setting project.templateFile = " + templateFile);
    p.setTemplateFile(templateFile.getAbsolutePath());
  }

  public void setProjectDataFile(File dataFile) {
    Project p = projectCombo.getValue();
    if (p == null) {
      return;
    }
    System.out.println("Setting project.datafile = " + dataFile);
    p.setDataFile(dataFile.getAbsolutePath());
  }

  public void setProjectDependencies(List<File> items) {
    Project p = projectCombo.getValue();
    if (p == null) {
      return;
    }
    System.out.println("Setting project.dependencies = " + items);
    p.setDependencies(items.stream().map(File::getAbsolutePath).collect(Collectors.toList()));
  }
}
