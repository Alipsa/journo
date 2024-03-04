package se.alipsa.journo.viewer;

import javafx.scene.control.Tab;

public abstract class JournoTab extends Tab {

  protected JournoViewer gui;
  public JournoTab(JournoViewer gui) {
    this.gui = gui;
  }

  protected void setStatus(String text) {
    gui.setStatus(text);
  }
}
