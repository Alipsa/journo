package se.alipsa.journo.viewer;

import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;

import java.io.File;
import java.nio.file.Path;

public abstract class JournoTab extends Tab {

  protected File file = null;
  protected boolean isChanged = false;

  private Tooltip saveToolTip = new Tooltip("Save");
  protected JournoViewer gui;
  public JournoTab(JournoViewer gui, String title) {
    this.gui = gui;
    defaultTitle = title;
    setTitle(title);
  }

  protected String defaultTitle;

  protected void setStatus(String text) {
    gui.setStatus(text);
  }

  public abstract CodeTextArea getCodeArea();

  public void contentChanged() {
    if (!getTitle().endsWith("*") && !isChanged) {
      setTitle(getTitle() + "*");
      isChanged = true;
      gui.disableRunButton();
    }
  }

  public void contentSaved() {
    setTitle(getTitle().replace("*", ""));
    isChanged = false;
    gui.enableRunButton();
  }

  public String getTitle() {
    return getText();
  }

  public void setTitle(String title) {
    setText(title);
    saveToolTip.setText("Save " + title.replace("*", ""));
  }

  public boolean isChanged() {
    return isChanged;
  }

  public abstract void promptAndLoad();

  public abstract void save();

  public void setFile(File file) {
    this.file = file;
    if (file == null) {
      setTitle(defaultTitle);
    }
  }

  public void setFile(Path dataFile) {
    if (dataFile != null) {
      file = dataFile.toFile();
    }
  }

  public abstract void clear();
}
