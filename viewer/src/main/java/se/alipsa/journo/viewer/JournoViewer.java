package se.alipsa.journo.viewer;

import freemarker.template.TemplateException;
import groovy.lang.GroovySystem;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.script.ScriptException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.prefs.Preferences;

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

    root.setTop(createMenu());

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

  public Window getStage() {
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
}
