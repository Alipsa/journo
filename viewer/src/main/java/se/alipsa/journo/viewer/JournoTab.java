package se.alipsa.journo.viewer;

import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;

public abstract class JournoTab extends Tab {

  protected boolean isChanged = false;

  private Tooltip saveToolTip = new Tooltip("Save");
  protected JournoViewer gui;
  public JournoTab(JournoViewer gui) {
    this.gui = gui;
  }

  protected void setStatus(String text) {
    gui.setStatus(text);
  }

  public abstract CodeTextArea getCodeArea();

  public void contentChanged() {
    if (!getTitle().endsWith("*") && !isChanged) {
      setTitle(getTitle() + "*");
      isChanged = true;
    }
  }

  public void contentSaved() {
    setTitle(getTitle().replace("*", ""));
    isChanged = false;
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
}
