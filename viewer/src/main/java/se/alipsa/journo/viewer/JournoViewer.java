package se.alipsa.journo.viewer;

import freemarker.template.TemplateException;
import groovy.lang.GroovySystem;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.FileAppender;
import se.alipsa.journo.JournoException;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static se.alipsa.journo.viewer.CreateProjectDialog.KEY_NAME;
import static se.alipsa.journo.viewer.CreateProjectDialog.KEY_PATH;

public class JournoViewer extends Application {

  private static Logger logger = LogManager.getLogger(JournoViewer.class);

  private PDFViewer pdfViewer;
  FreemarkerTab freeMarkerTab;
  Tab pdfTab;
  GroovyTab codeTab;
  private final TabPane tabPane = new TabPane();

  private Stage searchWindow;

  private final TextField statusField = new TextField();
  private Stage stage;
  private Scene scene;
  private final ComboBox<Project> projectCombo = new ComboBox<>();

  Label projectLabel = new Label("Project");
  private final List<String> searchStrings = new UniqueList<>();
  private static Image appIcon;

  private static URL styleSheetUrl;

  private Button viewPdfButton;
  private Button viewHtmlButton;
  private Button viewExternalButton;

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

    disableRunButton();
    scene = new Scene(root, 990, 980);
    scene.getStylesheets().add(getStyleSheet().toExternalForm());
    appIcon = getLogo();

    primaryStage.setOnCloseRequest(t -> {
      if (hasUnsavedFiles()) {
        boolean exitAnyway = Alerts.confirm(
            "Are you sure you want to exit?",
            "There are unsaved files",
            "Are you sure you want to exit \n -even though you have unsaved files?"
        );
        if (!exitAnyway) {
          t.consume();
          return;
        }
      }
      endProgram();
    });

    primaryStage.getIcons().add(appIcon);
    primaryStage.setResizable(true);
    primaryStage.setTitle("Journo Viewer");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static Image getLogo() {
    if (appIcon == null) {
      try (InputStream is = JournoViewer.class.getResourceAsStream("/journo-logo.png")) {
        appIcon = is == null ? null : new Image(is);
      } catch (Exception e) {
        // ignore
      }
    }
    return appIcon;
  }

  public static URL getStyleSheet() {
    if (styleSheetUrl == null) {
      styleSheetUrl = JournoViewer.class.getResource("/default-theme.css");
    }
    return styleSheetUrl;
  }

  private boolean hasUnsavedFiles() {
    for (Tab tab : tabPane.getTabs()) {
      if (tab instanceof JournoTab taTab) {
        if (taTab.isChanged()) {
          return true;
        }
      }
    }
    return false;
  }

  public void endProgram() {
    Platform.exit();
    // Allow some time before calling system exist so stop() can be used to do stuff if needed
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
      public void run() {
        System.exit(0);
      }
    };
    timer.schedule(task, 200);
  }

  private Node createProjectBar() {
    HBox hbox = new HBox();
    hbox.setSpacing(5);
    hbox.setPadding(new Insets(3,2,0,2));
    hbox.setStyle("-fx-border-color: lightgray");

    viewPdfButton = new Button("View PDF");
    viewPdfButton.setOnAction(a -> run());

    viewHtmlButton = new Button("View HTML");
    viewHtmlButton.setTooltip(new Tooltip("View html in Javafx WebView"));
    viewHtmlButton.setOnAction(a -> viewHtml());

    viewExternalButton = new Button("View external");
    viewExternalButton.setTooltip(new Tooltip("View in default PDF viewer"));
    viewExternalButton.setOnAction(a -> viewExternal());

    projectLabel.setStyle("-fx-background-color: transparent;");
    projectLabel.setPadding(new Insets(4, 0, 0, 5));
    projectLabel.setAlignment(Pos.BOTTOM_CENTER);
    try {
      populateProjectCombo(projectCombo);
    } catch (Exception e) {
      ExceptionAlert.showAlert("Failed to load project from preferences", e);
    }
    projectCombo.setOnAction(a -> setActiveProject(projectCombo.getValue()));
    Button saveButton = new Button("save");
    saveButton.setOnAction(a -> {
      if (getActiveProject() != null) {
        try {
          if (freeMarkerTab.isChanged()) {
            freeMarkerTab.save();
          }
          if (codeTab.isChanged()) {
            codeTab.save();
          }
          saveProject(projectCombo.getValue());
        } catch (IOException e) {
          ExceptionAlert.showAlert("Failed to save project", e);
        }
      } else {
        CreateProjectDialog cpd = new CreateProjectDialog(this);
        Optional<Map<String, String>> response = cpd.showAndWait();
        if (response.isEmpty()) {
          return;
        }
        Project p = new Project();
        Map<String, String> res = response.get();
        p.setName(res.get(KEY_NAME));
        try {
          saveProject(p, res.get(KEY_PATH));
          projectCombo.getItems().add(p);
          projectCombo.setValue(p);
          setActiveProject(p);
        } catch (IOException e) {
          ExceptionAlert.showAlert("Failed to save project " + p, e);
        }
        if (freeMarkerTab.isChanged()) {
          freeMarkerTab.save();
        }
        if (codeTab.isChanged()) {
          codeTab.save();
        }
      }
    });
    hbox.getChildren().addAll(saveButton, viewPdfButton, viewHtmlButton, viewExternalButton, projectLabel, projectCombo);
    return hbox;
  }

  private void createProject() {
    CreateProjectDialog cpd = new CreateProjectDialog(this);
    Optional<Map<String, String>> response = cpd.showAndWait();
    if (response.isEmpty()) {
      return;
    }
    Project p = new Project();
    Map<String, String> res = response.get();
    p.setName(res.get(KEY_NAME));
    freeMarkerTab.clear();
    codeTab.clear();
    try {
      saveProject(p, res.get(KEY_PATH));
      projectCombo.getItems().add(p);
      projectCombo.setValue(p);
      setActiveProject(p);
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to save project " + p, e);
    }
  }

  private void setActiveProject(Project p) {
    logger.info("Setting active project to {}: {}", p, p.values());
    freeMarkerTab.loadFile(p.getTemplateFile());
    codeTab.loadFile(p.getDataFile());
    logger.debug("setActiveProject(), Dependencies are: {}", p.getDependencies());
    codeTab.setDependencies(p.getDependencies());
    Preferences projects = preferences().node("projects");
    String path = projects.node(p.getName()).get("projectFile", null);
    if (path != null) {
      Path projectFilePath = Paths.get(path);
      setProjectDir(projectFilePath.getParent().toFile());
      projectCombo.setTooltip(new Tooltip(projectFilePath.toString()));
    } else {
      Alerts.warn("BUG! Missing project path", "Project path should have been saved but was null");
    }
  }

  private MenuBar createMenu() {
    MenuBar menuBar = new MenuBar();
    menuBar.setPadding(new Insets(5));

    Menu fileMenu = new Menu("File");
    menuBar.getMenus().add(fileMenu);
    MenuItem loadMi = new MenuItem("load");
    loadMi.setOnAction(a -> loadTabContent());
    MenuItem saveMi = new MenuItem("save");
    saveMi.setOnAction(a -> saveTabContent());
    MenuItem saveAsMi = new MenuItem("save as");
    saveAsMi.setOnAction(a -> saveAsTabContent());
    fileMenu.getItems().addAll(loadMi, saveMi, saveAsMi);

    Menu projectMenu = new Menu("Project");
    menuBar.getMenus().add(projectMenu);
    MenuItem saveProjectMi = new MenuItem("save");
    saveProjectMi.setOnAction(a -> {
      if (getActiveProject() != null) {
        try {
          saveProject(getActiveProject());
        } catch (IOException e) {
          ExceptionAlert.showAlert("Failed to save project", e);
        }
      }
    });
    MenuItem openProjectMi = new MenuItem("open");
    openProjectMi.setOnAction(a -> openProject());
    MenuItem newProjectMi = new MenuItem("new");
    newProjectMi.setOnAction(a -> createProject());
    projectMenu.getItems().addAll(saveProjectMi, openProjectMi, newProjectMi);



    Menu graphicsMenu = new Menu("Graphics");
    menuBar.getMenus().add(graphicsMenu);
    MenuItem addSvgTabMi = new MenuItem("Add SVG tab");
    graphicsMenu.getItems().add(addSvgTabMi);
    addSvgTabMi.setOnAction(a -> {
      SvgTab svgTab = new SvgTab(this);
      tabPane.getTabs().add(svgTab);
      tabPane.getSelectionModel().select(svgTab);
    });

    Menu editMenu = new Menu("Edit");
    menuBar.getMenus().add(editMenu);

    MenuItem editProjectMi = new MenuItem("edit project");
    editMenu.getItems().add(editProjectMi);
    editProjectMi.setOnAction(a -> editProject());

    MenuItem undoMI = new MenuItem("undo  ctrl+Z");
    editMenu.getItems().add(undoMI);
    undoMI.setOnAction(a -> undo());

    MenuItem redoMI = new MenuItem("redo ctrl+Y");
    editMenu.getItems().add(redoMI);
    redoMI.setOnAction(a -> redo());

    MenuItem findMI = new MenuItem("find ctrl+F");
    editMenu.getItems().add(findMI);
    findMI.setOnAction(a -> displayFind());

    Menu helpMenu = new Menu("Help");
    menuBar.getMenus().add(helpMenu);

    MenuItem viewLogFile = new MenuItem("View logfile");
    helpMenu.getItems().add(viewLogFile);
    viewLogFile.setOnAction(this::viewLogFile);

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

  private void openProject() {
    FileChooser fc = new FileChooser();
    fc.setInitialDirectory(getProjectDir());
    fc.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Journo project files", ".jpr"));
    File projectFile = fc.showOpenDialog(getStage());
    if (projectFile != null) {
      try {
        Project p = Project.load(projectFile.toPath());
        projectCombo.getItems().add(p);
        projectCombo.setValue(p);
        Preferences projects = preferences().node("projects");
        projects.node(p.getName()).put("projectFile", projectFile.toPath().toString());
        setActiveProject(p);
      } catch (Exception e) {
        ExceptionAlert.showAlert("Failed to load " + projectFile, e);
      }
    }
  }

  private void saveAsTabContent() {
    Tab tab = tabPane.getSelectionModel().getSelectedItem();
    if (tab instanceof JournoTab journoTab) {
      journoTab.setFile((File)null);
      journoTab.save();
    }
  }

  private void saveTabContent() {
    Tab tab = tabPane.getSelectionModel().getSelectedItem();
    if (tab instanceof JournoTab journoTab) {
      journoTab.save();
    }
  }

  private void loadTabContent() {
    Tab tab = tabPane.getSelectionModel().getSelectedItem();
    if (tab instanceof JournoTab journoTab) {
      journoTab.promptAndLoad();
    }
  }

  private void editProject() {
    Project p = projectCombo.getValue();
    if (p == null) {
      Alerts.info("Edit project", "Please select a project first");
      return;
    }
    EditProjectDialog editProjectDialog = new EditProjectDialog(p);
    Optional<Boolean> isChanged = editProjectDialog.showAndWait();
    if(isChanged.isPresent() && isChanged.get()) {
      freeMarkerTab.setFile(p.getTemplateFile());
      codeTab.setFile(p.getDataFile());
    }
  }

  private JournoTab getActiveTab() {
    return (JournoTab) tabPane.getSelectionModel().getSelectedItem();
  }

  private void undo() {
    JournoTab codeTab = getActiveTab();
    CodeTextArea codeArea = codeTab.getCodeArea();
    codeArea.undo();
  }

  private void redo() {
    JournoTab codeTab = getActiveTab();
    CodeTextArea codeArea = codeTab.getCodeArea();
    codeArea.redo();
  }

  public void displayFind() {
    if (searchWindow != null) {
      searchWindow.toFront();
      searchWindow.requestFocus();
      return;
    }

    VBox vBox = new VBox();
    vBox.setPadding(new Insets(3));
    FlowPane pane = new FlowPane();
    vBox.getChildren().add(pane);
    Label resultLabel = new Label();
    resultLabel.setPadding(new Insets(1));
    vBox.getChildren().add(resultLabel);
    pane.setPadding(new Insets(5));
    pane.setHgap(5);
    pane.setVgap(5);
    Button findButton = new Button("search");

    ComboBox<String> searchInput = new ComboBox<>();
    searchInput.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        findButton.fire();
      }
    });
    searchInput.setEditable(true);
    if (!searchStrings.isEmpty()) {
      searchStrings.forEach(s -> searchInput.getItems().add(s));
      searchInput.setValue(searchStrings.get(searchStrings.size()-1));
    }

    findButton.setOnAction(e -> {
      JournoTab codeTab = getActiveTab();
      if (codeTab == null) {
        resultLabel.setText("No active code tab exists, nothing to search in");
        return;
      }
      CodeTextArea codeArea = codeTab.getCodeArea();
      int caretPos = codeArea.getCaretPosition();
      String text = codeArea.getText().substring(caretPos);
      String searchWord = searchInput.getValue();
      if (searchWord == null) {
        searchWord = searchInput.getEditor().getText();
        if (searchWord == null) {
          logger.info("searchWord is null and nothing entered in the combobox text field, nothing that can be searched");
          resultLabel.setText("Nothing to search for");
          return;
        }
      }
      searchStrings.add(searchWord);
      if (!searchInput.getItems().contains(searchWord)) {
        searchInput.getItems().add(searchWord);
      }
      if (text.contains(searchWord)) {
        int place = text.indexOf(searchWord);
        codeArea.moveTo(place);
        codeArea.selectRange(caretPos + place, caretPos + place + searchWord.length());
        codeArea.requestFollowCaret();
        resultLabel.setText("found on line " + (codeArea.getCurrentParagraph() + 1));
      } else {
        resultLabel.setText(searchWord + " not found");
      }
    });

    Button toTopButton = new Button("To beginning");
    toTopButton.setOnAction(a -> {
      JournoTab codeTab = getActiveTab();
      CodeTextArea codeArea = codeTab.getCodeArea();
      codeArea.moveTo(0);
      codeArea.requestFollowCaret();
    });
    pane.getChildren().addAll(searchInput, findButton, toTopButton);
    Scene scene = new Scene(vBox);
    scene.getStylesheets().addAll(scene.getStylesheets());
    searchWindow = new Stage();
    searchWindow.setOnCloseRequest(event -> searchWindow = null);
    searchWindow.setTitle("Find");
    searchWindow.setScene(scene);
    searchWindow.sizeToScene();
    searchWindow.show();
    searchWindow.toFront();
    searchWindow.setAlwaysOnTop(true);

  }

  void populateProjectCombo(ComboBox<Project> projectCombo) throws BackingStoreException, IOException {
    Preferences projects = preferences().node("projects");
    ObservableList<Project> list = projectCombo.getItems();
    for (String name : projects.childrenNames()) {
      String path = projects.node(name).get("projectFile", null);
      if (path == null) {
        projects.node(name).removeNode();
        continue;
      }
      Path projectFilePath = Paths.get(path);
      if (Files.exists(projectFilePath)) {
        try {
          list.add(Project.load(projectFilePath));
        } catch (Exception e) {
          ExceptionAlert.showAlert("Failed to load project from " + projectFilePath, e);
        }
      } else {
        logger.info("{} does not exist, removing the projectFile pref", projectFilePath);
        projects.node(name).removeNode();
      }
    }
  }

  void saveProject(Project p, String path) throws IOException {
    Preferences projects = preferences().node("projects");
    projects.node(p.getName()).put("projectFile", path);
    Project.save(p, Paths.get(path));
  }

  void saveProject(Project p) throws IOException {
    Preferences projects = preferences().node("projects");
    String projectFilePref = projects.node(p.getName()).get("projectFile", null);
    Path projectFilePath;
    if (projectFilePref == null) {
      FileChooser fc = new FileChooser();
      fc.setTitle("Save Journo project file");
      fc.setInitialDirectory(getProjectDir());
      fc.setInitialFileName(p.getName() + ".jpr");
      File file = fc.showSaveDialog(getStage());
      if (file == null) {
        return;
      }
      projectFilePath = file.toPath();
    } else {
      projectFilePath = Paths.get(projectFilePref);
    }
    logger.debug("Saving project: " + p.values());
    Project.save(p, projectFilePath);
    projects.node(p.getName()).put("projectFile", projectFilePath.toString());
  }


  void disableRunButton() {
    viewPdfButton.setDisable(true);
    viewHtmlButton.setDisable(true);
    viewExternalButton.setDisable(true);
  }

  void enableRunButton() {
    if (!freeMarkerTab.isChanged() && !codeTab.isChanged()) {
      viewPdfButton.setDisable(false);
      viewHtmlButton.setDisable(false);
      viewExternalButton.setDisable(false);
    }
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
      fc.setInitialDirectory(getProjectDir());
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

    pdfViewer = new PDFViewer(this);
    root.setCenter(pdfViewer);

    pdfTab.setContent(root);
    return pdfTab;
  }

  void run() {
    scene.setCursor(Cursor.WAIT);
    try {
      Map<String, Object> data = codeTab.runScript(true);
      // TODO: Check if templateArea is saved and if not save if loaded from file or prompt to save to new file
      byte[] pdf = freeMarkerTab.renderPdf(data);
      pdfViewer.load(pdf);
      tabPane.getSelectionModel().select(pdfTab);
      scene.setCursor(Cursor.DEFAULT);
    } catch (IOException | JournoException e) {
      scene.setCursor(Cursor.DEFAULT);
      ExceptionAlert.showAlert("Failed to render the pdf", e);
    }  catch (Throwable e) {
      scene.setCursor(Cursor.DEFAULT);
      ExceptionAlert.showAlert("Failed to execute groovy script", e);
    }
  }

  void viewHtml() {
    scene.setCursor(Cursor.WAIT);
    try {
      Map<String, Object> data = codeTab.runScript(true);
      // TODO: Check if templateArea is saved and if not save if loaded from file or prompt to save to new file
      String html = freeMarkerTab.renderHtml(data);
      WebView webView = new WebView();
      webView.getEngine().loadContent(html);
      Popup.display(webView, this);
      scene.setCursor(Cursor.DEFAULT);
    } catch (JournoException e) {
      scene.setCursor(Cursor.DEFAULT);
      ExceptionAlert.showAlert("Failed to render the pdf", e);
    }  catch (Throwable e) {
      scene.setCursor(Cursor.DEFAULT);
      ExceptionAlert.showAlert("Failed to execute groovy script", e);
    }
  }

  void viewExternal() {
    scene.setCursor(Cursor.WAIT);
    try {
      Map<String, Object> data = codeTab.runScript(true);
      // TODO: Check if templateArea is saved and if not save if loaded from file or prompt to save to new file
      File file = File.createTempFile(projectCombo.getValue().getName(), ".pdf");
      freeMarkerTab.renderPdf(data, file);
      openInExternalApp(file);
      file.deleteOnExit();
      scene.setCursor(Cursor.DEFAULT);
    } catch (IOException | JournoException e) {
      scene.setCursor(Cursor.DEFAULT);
      ExceptionAlert.showAlert("Failed to render the pdf", e);
    } catch (Throwable e) {
      scene.setCursor(Cursor.DEFAULT);
      ExceptionAlert.showAlert("Failed to execute groovy script", e);
    }
  }

  private void openInExternalApp(File file) {
    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws Exception {
        Desktop.getDesktop().open(file);
        return null;
      }
    };
    task.setOnFailed(e -> ExceptionAlert.showAlert("Failed to open " + file, task.getException()));
    Thread appthread = new Thread(task);
    appthread.start();
  }

  public void setStatus(String status) {
    statusField.setText(status);
  }

  public static Path writeToFile(File file, byte[] content) throws IOException {
    return Files.write(file.toPath(), content);
  }

  public void setProjectTemplateFile(Path templateFile) {
    Project p = projectCombo.getValue();
    if (p == null) {
      return;
    }
    logger.debug("Setting project.templateFile = " + templateFile);
    p.setTemplateFile(templateFile);
  }

  public void setProjectDataFile(Path dataFile) {
    Project p = projectCombo.getValue();
    if (p == null) {
      return;
    }
    logger.debug("Setting project.datafile = " + dataFile);
    p.setDataFile(dataFile);
  }

  public void setProjectDependencies(List<Path> items) {
    Project p = projectCombo.getValue();
    if (p == null) {
      return;
    }
    logger.debug("Setting project.dependencies = " + items);
    p.setDependencies(items);
  }

  private void viewLogFile(ActionEvent actionEvent) {
    try {
      org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
      Map.Entry<String, Appender> appenderEntry = logger.get().getAppenders().entrySet().stream()
          .filter(e -> "JournoLog".equals(e.getKey())).findAny().orElse(null);
      if (appenderEntry == null) {
        Alerts.warn("Failed to find log file", "Failed to find an appender called JournoLog");
        return;
      }
      FileAppender appender = (FileAppender) appenderEntry.getValue();

      File logFile = new File(appender.getFileName());
      if (!logFile.exists()) {
        Alerts.warn("Failed to find log file", "Failed to find log file " + logFile.getAbsolutePath());
        return;
      }
      try {
        String content = Files.readString(logFile.toPath());
        Alerts.info(logFile.getAbsolutePath(), content);
      } catch (IOException e) {
        ExceptionAlert.showAlert("Failed to read log file content", e);
      }
    } catch (RuntimeException e) {
      ExceptionAlert.showAlert("Failed to show log file", e);
    }
  }

  public void saveDataFileToProject(File groovyFile) {
    Project p = projectCombo.getValue();
    if (p != null && groovyFile != null) {
      p.setDataFile(groovyFile.toPath());
    }
  }

  public void saveTemplateFileToProject(File markupFile) {
    Project p = projectCombo.getValue();
    if (p != null && markupFile != null) {
      p.setTemplateFile(markupFile.toPath());
    }
  }

  public File getProjectDir() {
    File dir = new File(System.getProperty("user.dir"));
    if (!dir.exists()) {
      String projectFilePref = preferences().node(projectCombo.getValue().getName()).get("projectFile", null);
      if (projectFilePref != null) {
        return new File(projectFilePref).getParentFile();
      }
    } else if (dir.isFile()) {
      return dir.getParentFile();
    }
    return dir;
  }

  public void setProjectDir(File dir) {
    if (dir.isFile()) {
      dir = dir.getParentFile();
    }
    System.setProperty("user.dir", dir.getAbsolutePath());
  }

  public Project getActiveProject() {
    return projectCombo.getValue();
  }
}
