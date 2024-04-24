package se.alipsa.journo.viewer;

import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class GroovyTab extends JournoTab {

  private static final Logger log = LogManager.getLogger(GroovyTab.class);
  private final GroovyTextArea codeArea;
  private final ListView<Path> jarDependencies;

  public GroovyTab(JournoViewer gui) {
    super(gui, "Data");
    setClosable(false);
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
    jarDependencies.getItems().addListener((ListChangeListener<? super Path>) c -> {
      codeArea.setDependencies(jarDependencies.getItems());
    });
    VBox.setVgrow(jarDependencies, Priority.ALWAYS);
    dependenciesBox.getChildren().addAll(depLabel, jarDependencies);
    root.setRight(dependenciesBox);

    HBox actionPane = new HBox();
    actionPane.setPadding(new Insets(5));
    actionPane.setSpacing(5);

    Button showResultButton = new Button("Run script");
    showResultButton.setOnAction(a -> {
      TextArea ta = new TextArea();
      StringBuilder sb = new StringBuilder();
      try {
        Object result = codeArea.executeGroovyScript();
        if (result instanceof Map map) {
          sb.append("[\n");
          map.forEach((k, v) -> sb.append(k).append(": ").append(v).append('\n'));
          sb.append("]");
        } else {
          sb.append(result);
        }
        ta.setText(sb.toString());
        Popup.display(ta, gui, "Groovy script result");
      } catch (Exception e) {
        ExceptionAlert.showAlert("Failed to run Script", e);
      }
    });
    actionPane.getChildren().addAll(showResultButton);
    root.setBottom(actionPane);

    setContent(root);
  }

  private void loadScript(File targetFile) {
    try {
      codeArea.setText(Files.readString(targetFile.toPath()));
      file = targetFile;
      setText(targetFile.getName());
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to read " + file, e);
    }
  }

  public void loadFile(Path dataFile) {
    if(dataFile == null) {
      return;
    }
    loadScript(dataFile.toFile());
  }

  private ContextMenu createOutsideContextMenu(ListView<Path> dependencies) {
    ContextMenu outsideContextMenu = new ContextMenu();
    MenuItem addDependencyMI = new MenuItem("add");
    addDependencyMI.setOnAction(a -> {
      FileChooser fc = new FileChooser();
      fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Jar file", "*.jar"));
      fc.setTitle("Add jar dependency");
      fc.setInitialDirectory(gui.getProjectDir());
      File jarFile = fc.showOpenDialog(gui.getStage());
      if (jarFile != null) {
        dependencies.getItems().add(jarFile.toPath());
        gui.setProjectDependencies(dependencies.getItems());
      }
    });
    outsideContextMenu.getItems().add(addDependencyMI);
    return outsideContextMenu;
  }

  public Map<String, Object> runScript(boolean... entireScript) {
    try {
      Object result = codeArea.executeGroovyScript(entireScript);
      if (result instanceof Map) {
        return (Map<String, Object>)result;
      } else {
        Alerts.warn(
            "The script does not return a Map<String, Object> but a " + result.getClass(),
            String.valueOf(result)
        );
      }
    } catch (Exception e) {
      ExceptionAlert.showAlert("Failed to run groovy script", e);
    }
    return Collections.emptyMap();
  }

  public void setDependencies(Collection<Path> dependencyList) {
    jarDependencies.getItems().clear();
    dependencyList.forEach(d -> jarDependencies.getItems().add(d));
  }

  private class FileCell extends ListCell<Path> {

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
    protected void updateItem(Path item, boolean empty) {
      super.updateItem(item, empty);
      setText(item == null ? "" : item.getFileName().toString());
    }
  }

  @Override
  public CodeTextArea getCodeArea() {
    return codeArea;
  }

  @Override
  public void promptAndLoad() {
    FileChooser fc = new FileChooser();
    fc.setTitle("Select Groovy Script");
    fc.setInitialDirectory(gui.getProjectDir());
    File targetFile = fc.showOpenDialog(gui.getStage());
    if (targetFile != null) {
      loadScript(targetFile);
      gui.setProjectDataFile(targetFile.toPath());
    }
  }

  @Override
  public void save() {
    if (file != null) {
      try {
        Files.writeString(file.toPath(), codeArea.getText());
        setStatus("Saved " + file);
        contentSaved();
        gui.saveDataFileToProject(file);
      } catch (IOException e) {
        setStatus("Failed to write " + file);
        ExceptionAlert.showAlert("Failed to write " + file, e);
      }
    } else {
      log.info("Saving groovy script to new location");
      FileChooser fc = new FileChooser();
      fc.setTitle("Save groovy script");
      fc.setInitialDirectory(gui.getProjectDir());

      String projectName = gui.getActiveProject().getName();
      if (projectName != null) {
        String suggested = projectName + ".groovy";
        fc.setInitialFileName(suggested);
      }
      File targetFile = fc.showSaveDialog(gui.getStage());

      if (targetFile != null) {
        Path filePath = targetFile.toPath();
        try {
          setStatus("Writing " + filePath.toAbsolutePath());
          Files.writeString(filePath, codeArea.getText());
          setFile(targetFile);
          setText(targetFile.getName());
          contentSaved();
          gui.saveDataFileToProject(file);
          setStatus("Saved " + file);
        } catch (Exception e) {
          setStatus("Failed to write " + file);
          ExceptionAlert.showAlert("Failed to write " + filePath, e);
        }
      }
    }
  }

  @Override
  public void clear() {
    file = null;
    setTitle(defaultTitle);
    codeArea.clear();
  }
}
