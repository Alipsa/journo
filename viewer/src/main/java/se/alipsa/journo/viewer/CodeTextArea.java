package se.alipsa.journo.viewer;

import javafx.scene.control.IndexRange;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public abstract class CodeTextArea extends CodeArea {
  public static final String INDENT = "  ";

  protected JournoViewer gui;
  protected CodeTextArea(JournoViewer gui) {
    this.gui = gui;
    getStylesheets().clear();
    getStyleClass().add("styled-text-area");
    getStyleClass().add("code-area");
    getStyleClass().add("codeTextArea");
    setUseInitialStyleForInsertion(true);
    setParagraphGraphicFactory(LineNumberFactory.get(this));
    multiPlainChanges()
        // do not emit an event until 400 ms have passed since the last emission of previous stream
        .successionEnds(Duration.ofMillis(400))
        // run the following code block when previous stream emits an event
        .subscribe(ignore -> highlightSyntax());

    InputMap<KeyEvent> im = InputMap.consume(
        EventPattern.keyPressed(KeyCode.TAB),
        e -> {
          String selected = selectedTextProperty().getValue();
          if (!"".equals(selected)) {
            IndexRange range = getSelection();
            int start = range.getStart();
            String indented = indentText(selected);
            replaceSelection(indented);
            selectRange(start, start + indented.length());
          } else {
            String line = getText(getCurrentParagraph());
            int orgPos = getCaretPosition();
            moveTo(getCurrentParagraph(), 0);
            int start = getCaretPosition();
            int end = start + line.length();
            replaceText(start, end, INDENT + line);
            moveTo(orgPos + INDENT.length());
          }
        }
    );
    Nodes.addInputMap(this, im);
    addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      KeyCode keyCode = e.getCode();
      if (e.isControlDown() || e.isMetaDown()) {
        if (KeyCode.F.equals(keyCode)) {
          gui.displayFind();
        }
      }
    });

    plainTextChanges().subscribe(ptc -> {
      gui.contentChanged();
    });
  }

  public abstract void highlightSyntax();

  /**
   * Indent (add space) to the selected area
   * e.g. when pressing the tab button
   *
   * @param selected the text to indent
   * @return the indented text
   */
  protected String indentText(String selected) {
    if (selected == null || "".equals(selected)) {
      return INDENT;
    }
    String[] lines = selected.split("\n");
    List<String> tabbed = new ArrayList<>();
    for (String line : lines) {
      tabbed.add(INDENT + line);
    }
    return String.join("\n", tabbed);
  }

}
