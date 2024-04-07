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

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class GroovyTab extends JournoTab {

  private static final Logger log = LogManager.getLogger(GroovyTab.class);
  private File groovyFile = null;
  private final Button codeRunButton;
  private final GroovyTextArea codeArea;
  private final ListView<Path> jarDependencies;

  public GroovyTab(JournoViewer gui) {
    super(gui);
    setText("Data");
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
    Button loadScriptButton = new Button("Load groovy script");
    loadScriptButton.setOnAction(a -> {
      FileChooser fc = new FileChooser();
      fc.setTitle("Select Groovy Script");
      fc.setInitialDirectory(gui.getProjectDir());
      File targetFile = fc.showOpenDialog(gui.getStage());
      if (targetFile != null) {
        loadScript(targetFile);
        gui.setProjectDataFile(targetFile.toPath());
      }
    });
    Button saveScriptButton = new Button("Save groovy script");
    saveScriptButton.setOnAction(a -> {
      if (groovyFile != null) {
        try {
          Files.writeString(groovyFile.toPath(), codeArea.getText());
          setStatus("Saved " + groovyFile);
          contentSaved();
          gui.saveDataFileToProject(groovyFile);
        } catch (IOException e) {
          setStatus("Failed to write " + groovyFile);
          ExceptionAlert.showAlert("Failed to write " + groovyFile, e);
        }
      } else {
        log.info("Saving groovy script to new location");
        FileChooser fc = new FileChooser();
        fc.setTitle("Save groovy script");
        fc.setInitialDirectory(gui.getProjectDir());

        String template = gui.getSelectedTemplate();
        if (template != null) {
          String suggested = template.substring(0, template.lastIndexOf(".")) + ".groovy";
          fc.setInitialFileName(suggested);
        }
        File targetFile = fc.showSaveDialog(gui.getStage());

        if (targetFile != null) {
          Path filePath = targetFile.toPath();
          try {
            setStatus("Writing " + filePath.toAbsolutePath());
            Files.writeString(filePath, codeArea.getText());
            setStatus("Saved " + groovyFile);
            groovyFile = targetFile;
            setText(targetFile.getName());
            contentSaved();
            gui.saveDataFileToProject(groovyFile);
          } catch (Exception e) {
            setStatus("Failed to write " + groovyFile);
            ExceptionAlert.showAlert("Failed to write " + filePath, e);
          }
        }
      }
    });
    codeRunButton = new Button("View PDF");
    codeRunButton.setOnAction(a -> gui.run());

    Button showResultButton = new Button("Run script");
    showResultButton.setOnAction(a -> {
      TextArea ta = new TextArea();
      StringBuilder sb = new StringBuilder();
      try {
        runScript().forEach((k, v) -> sb.append(k).append(": ").append(v).append('\n'));
        ta.setText(sb.toString());
        Popup.display(ta, gui, "Groovy script result");
      } catch (Exception e) {
        ExceptionAlert.showAlert("Failed to run Script", e);
      }
    });
    actionPane.getChildren().addAll(loadScriptButton,saveScriptButton, codeRunButton, showResultButton);
    root.setBottom(actionPane);

    setContent(root);
  }

  private void loadScript(File targetFile) {
    try {
      codeArea.setText(Files.readString(targetFile.toPath()));
      groovyFile = targetFile;
      setText(targetFile.getName());
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to read " + groovyFile, e);
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
      File jarFile = fc.showOpenDialog(gui.getStage());
      if (jarFile != null) {
        dependencies.getItems().add(jarFile.toPath());
        gui.setProjectDependencies(dependencies.getItems());
      }
    });
    outsideContextMenu.getItems().add(addDependencyMI);
    return outsideContextMenu;
  }

  public void disbleRunButton() {
    codeRunButton.setDisable(true);
  }

  public void enableRunButton() {
    codeRunButton.setDisable(false);
  }

  public Map<String, Object> runScript() {
    try {
      return codeArea.executeGroovyScript();
    } catch (Exception e) {
      ExceptionAlert.showAlert("Failed to run groovy script", e);
      return Collections.emptyMap();
    }
  }

  public Path getScriptFile() {
    return groovyFile == null ? null : groovyFile.toPath();
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
}
