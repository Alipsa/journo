package se.alipsa.journo.viewer;

import groovy.lang.GroovyClassLoader;
import io.github.classgraph.*;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroovyTextArea extends CodeTextArea {

  private static Logger logger = LogManager.getLogger(GroovyTextArea.class);
  private GroovyScriptEngineImpl groovyScriptEngine;
  Map<String, Object> contextObjects = new HashMap<>();

  ContextMenu suggestionsPopup = new ContextMenu();

  private static final String[] KEYWORDS = new String[]{
          "abstract", "as", "assert",
          "boolean", "break", "byte",
          "case", "catch", "char", "class", "const", "continue",
          "def", "default", "do", "double",
          "else", "enum", "extends",
          "false", "final", "finally", "float", "for",
          "goto", "@Grab",
          "if", "implements", "import", "in", "instanceof", "int", "interface",
          "long",
          "native", "new", "null",
          "package", "private", "protected", "public",
          "return",
          "short", "static", "strictfp", "super", "switch", "synchronized",
          "this", "threadsafe", "throw", "throws",
          "transient", "true", "try",
          "var", "void", "volatile",
          "while"
  };

  private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
  private static final String PAREN_PATTERN = "\\(|\\)";
  private static final String BRACE_PATTERN = "\\{|\\}";
  private static final String BRACKET_PATTERN = "\\[|\\]";
  private static final String SEMICOLON_PATTERN = "\\;";
  private static final String STRING_PATTERN = "\"\"|''|\"[^\"]+\"|'[^']+'";
  private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

  private static final Pattern PATTERN = Pattern.compile(
      "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
          + "|(?<PAREN>" + PAREN_PATTERN + ")"
          + "|(?<BRACE>" + BRACE_PATTERN + ")"
          + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
          + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
          + "|(?<STRING>" + STRING_PATTERN + ")"
          + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
  );

  public GroovyTextArea(JournoTab parentTab) {
    super(parentTab);

    addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.isControlDown() && (KeyCode.SPACE.equals(e.getCode()) || KeyCode.PERIOD.equals(e.getCode()))) {
        autoComplete();
      }
    });
    //Platform.runLater(() -> setParagraphGraphicFactory(LineNumberFactory.get(this)));
  }

  /**
   * compute and set syntax highlighting
   */
  @Override
  public void highlightSyntax() {
    setStyleSpans(0, computeHighlighting(getText()));
  }

  protected final StyleSpans<Collection<String>> computeHighlighting(String text) {
    Matcher matcher = PATTERN.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder
        = new StyleSpansBuilder<>();
    while (matcher.find()) {
      String styleClass =
          matcher.group("KEYWORD") != null ? "keyword" :
              matcher.group("PAREN") != null ? "paren" :
                  matcher.group("BRACE") != null ? "brace" :
                      matcher.group("BRACKET") != null ? "bracket" :
                          matcher.group("SEMICOLON") != null ? "semicolon" :
                              matcher.group("STRING") != null ? "string" :
                                  matcher.group("COMMENT") != null ? "comment" :
                                      null; /* never happens */
      assert styleClass != null;
      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
      lastKwEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }

  public void autoComplete() {
    String line = getText(getCurrentParagraph());
    String currentText = line.substring(0, getCaretColumn());
    String lastWord;
    int index = currentText.indexOf(' ');
    if (index == -1 ) {
      lastWord = currentText;
    } else {
      lastWord = currentText.substring(currentText.lastIndexOf(' ') + 1);
    }
    index = lastWord.indexOf(',');
    if (index > -1) {
      lastWord = lastWord.substring(index+1);
    }
    index = lastWord.indexOf('(');
    if (index > -1) {
      lastWord = lastWord.substring(index+1);
    }
    index = lastWord.indexOf('[');
    if (index > -1) {
      lastWord = lastWord.substring(index+1);
    }
    index = lastWord.indexOf('{');
    if (index > -1) {
      lastWord = lastWord.substring(index+1);
    }

    //Gade.instance().getConsoleComponent().getConsole().appendFx("lastWord is " + lastWord, true);

    if (!lastWord.isEmpty()) {
      suggestCompletion(lastWord);
    }
  }

  private void suggestCompletion(String lastWord) {
    //console.appendFx("Getting suggestions for " + lastWord, true);
    TreeMap<String, Boolean> suggestions = new TreeMap<>();

    contextObjects = getContextObjects();

    for (Map.Entry<String, Object> contextObject: contextObjects.entrySet()) {
      String key = contextObject.getKey();
      if (key.equals(lastWord)) {
        suggestions.put(".", Boolean.FALSE);
      } else if (key.startsWith(lastWord)) {
        suggestions.put(key, Boolean.FALSE);
      } else if (lastWord.startsWith(key) && lastWord.contains(".")) {
        int firstDot = lastWord.indexOf('.');
        String varName = lastWord.substring(0, lastWord.indexOf('.'));
        if (key.equals(varName)){
          suggestions.putAll(getInstanceMethods(contextObject.getValue(), lastWord.substring(firstDot+1)));
        }
      }
    }
    if (!suggestions.isEmpty()) {
      suggestCompletion(lastWord, suggestions, suggestionsPopup);
      return;
    }

    // Else it is probably package or Class related
    String searchWord = lastWord;
    boolean endsWithDot = false;
    if (searchWord.endsWith(".")) {
      searchWord = searchWord.substring(0, searchWord.length() -1);
      endsWithDot = true;
    }
    ClassLoader cl = this.getClass().getClassLoader();
    try {
      Class<?> clazz = cl.loadClass(searchWord);
      suggestions.putAll(getStaticMethods(clazz));
    } catch (ClassNotFoundException e) {
      try (ScanResult scanResult = new ClassGraph().enableClassInfo().addClassLoader(cl).scan()) {
        String finalSearchWord = searchWord;
        List<? extends Class<?>> exactMatches = scanResult.getAllClasses().stream()
            .filter(ci -> ci.getSimpleName().equals(finalSearchWord)).map(ClassInfo::loadClass)
            .toList();
        if (exactMatches.size() == 1) {
          String prefix = endsWithDot ? "" : ".";
          lastWord = endsWithDot ? lastWord : lastWord + ".";
          suggestions.putAll(getStaticMethods(exactMatches.get(0), prefix));
        } else if (exactMatches.size() > 1){
          parentTab.gui.setStatus("Multiple matches for this class detected, cannot determine which one is meant");
          return;
        } else {
          List<String> possiblePackages = scanResult.getPackageInfo().stream()
              .map(PackageInfo::getName)
              .filter(name -> name.startsWith(finalSearchWord))
              .toList();
          if (!possiblePackages.isEmpty()) {
            Map<String, Boolean> packages = new TreeMap<>();
            lastWord = endsWithDot ? lastWord : lastWord + ".";
            for (String pkg : possiblePackages) {
              String suggestion = pkg.substring(finalSearchWord.length());
              if (endsWithDot && suggestion.startsWith(".")) {
                suggestion = suggestion.substring(1);
              }
              packages.put(suggestion, Boolean.FALSE);
            }
            suggestions.putAll(packages);
          }
        }
      }
    }
    if (!suggestions.isEmpty()) {
      suggestCompletion(lastWord, suggestions, suggestionsPopup);
    } else {
      parentTab.gui.setStatus("No matches found for " + searchWord);
    }
  }

  private Map<String, Boolean> getStaticMethods(Class<?> clazz, String... prefixOpt) {
    String prefix = prefixOpt.length > 0 ? prefixOpt[0] : "";
    Map<String, Boolean> staticMethods = new TreeMap<>();
    for(Method method : clazz.getMethods()) {
      if ( Modifier.isStatic(method.getModifiers())) {
        Boolean hasParams = method.getParameterCount() > 0;
        String suggestion = method.getName() + "()";
        if (Boolean.TRUE.equals(staticMethods.get(suggestion))) {
          hasParams = Boolean.TRUE;
        }
        staticMethods.put(prefix + suggestion, hasParams);
      }
    }
    return staticMethods;
  }

  private Map<String, Boolean> getInstanceMethods(Object obj, String start) {
    Map<String, Boolean> instanceMethods = new TreeMap<>();
    for(Method method : obj.getClass().getMethods()) {
      //Gade.instance().getConsoleComponent().getConsole().appendFx(method.getName() + " and startWith '" + start + "'");
      if ( !Modifier.isStatic(method.getModifiers()) && ("".equals(start) || method.getName().startsWith(start))) {
        Boolean hasParams = method.getParameterCount() > 0;
        String suggestion = method.getName() + "()";
        if (Boolean.TRUE.equals(instanceMethods.get(suggestion))) {
          hasParams = Boolean.TRUE;
        }
        instanceMethods.put(suggestion, hasParams);
      }
    }
    return instanceMethods;
  }

  public Map<String, Object> getContextObjects() {
    Map<String, Object> contextObjects = new HashMap<>(getGroovyEngine().getBindings(ScriptContext.ENGINE_SCOPE));
    return contextObjects;
  }

  private GroovyScriptEngineImpl getGroovyEngine() {
    if (groovyScriptEngine == null) {
      groovyScriptEngine = new GroovyScriptEngineImpl();
    }
    return groovyScriptEngine;
  }

  Map<String, Object> executeGroovyScript() throws Exception {
    Object result = getGroovyEngine().eval(getText());
    try {
      return (Map<String, Object>) result;
    } catch (ClassCastException e) {
      ExceptionAlert.showAlert("The script does not return a Map<String, Object> but a " + result.getClass(), e);
    }
    return null;
  }


  public void setDependencies(List<Path> dependencies) {
    // we reinitialize the scriptengine to handle removal of jars
    groovyScriptEngine = new GroovyScriptEngineImpl();
    final GroovyClassLoader groovyClassLoader = groovyScriptEngine.getClassLoader();
    dependencies.forEach(f -> {
      try {
        logger.debug("Adding " + f + " to classloader");
        groovyClassLoader.addURL(f.toUri().toURL());
      } catch (MalformedURLException e) {
        ExceptionAlert.showAlert("Failed to add jar " + f, e);
      }
    });
  }

  public void setText(String text) {
    blockChange = true;
    replaceText(text);
    highlightSyntax();
    blockChange = false;
  }

  protected void suggestCompletion(String lastWord, TreeMap<String, Boolean> keyWords, ContextMenu suggestionsPopup) {
    List<CustomMenuItem> menuItems = new LinkedList<>();
    for (Map.Entry<String, Boolean> entry : keyWords.entrySet()) {
      String result = entry.getKey();
      Label entryLabel = new Label(result);
      CustomMenuItem item = new CustomMenuItem(entryLabel, true);
      item.setOnAction(actionEvent -> {
        try {
          String replacement;
          if (lastWord.contains(".")) { // We are selecting from a list of methods
            String start = lastWord.substring(lastWord.lastIndexOf('.') + 1);
            //Gade.instance().getConsoleComponent().getConsole().appendFx("start is " + start + ", result is " + result, true);
            replacement = result.substring(start.length());
          } else { // We are completing a keyword or an object name
            replacement = result.substring(lastWord.length());
          }
          //Gade.instance().getConsoleComponent().getConsole().appendFx("result is " + result + ", replacement is " + replacement, true);
          insertText(getCaretPosition(), replacement);
          int currentParagraph = getCurrentParagraph();
          if (entry.getValue()) {
            int lineEnd = getParagraphLength(currentParagraph);
            int colIdx = replacement.endsWith(")") ? lineEnd - 1 : lineEnd;
            moveTo(currentParagraph, colIdx);
          } else {
            moveTo(currentParagraph, getParagraphLength(currentParagraph));
          }
          suggestionsPopup.hide();
          requestFocus();
        } catch (Throwable t) {
          ExceptionAlert.showAlert("Failed to process suggestion: " + t.getMessage(), t);
        }
      });
      menuItems.add(item);
    }
    suggestionsPopup.getItems().clear();
    suggestionsPopup.getItems().addAll(menuItems);
    double screenX = 0;
    double screenY = 0;
    Optional<Bounds> bounds = this.caretBoundsProperty().getValue();
    if (bounds.isPresent()) {
      Bounds bound = bounds.get();
      screenX = bound.getMaxX();
      screenY = bound.getMaxY();
    }
    suggestionsPopup.setOnHiding(e -> this.requestFocus());
    suggestionsPopup.show(this, screenX, screenY);
  }

  @Override
  public String getText() {
    String code;
    String selected = selectedTextProperty().getValue();
    if (selected == null || selected.isEmpty()) {
      code = textProperty().getValue();
    } else {
      code = selected;
    }
    return code;
  }

}
