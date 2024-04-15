package se.alipsa.journo.viewer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Project {

  private String name;
  private Path templateFile;
  private Path dataFile;

  private List<Path> dependencies = new ArrayList<>();

  public Project() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Path getTemplateFile() {
    return templateFile;
  }

  public void setTemplateFile(Path templateFile) {
    this.templateFile = templateFile;
  }

  public Path getDataFile() {
    return dataFile;
  }

  public void setDataFile(Path dataFile) {
    this.dataFile = dataFile;
  }

  public List<Path> getDependencies() {
    return dependencies;
  }

  public void setDependencies(Collection<Path> dependencies) {
    this.dependencies.clear();
    this.dependencies.addAll(dependencies);
  }
  public void setDependencies(String dependencyList, Path projectPath) {
    dependencies = new ArrayList<>();
    if (dependencyList != null && !dependencyList.isBlank()) {
      for (String dep : dependencyList.split(";")) {
        dependencies.add(absolutePath(Paths.get(dep), projectPath));
      }
    }
  }

  @Override
  public String toString() {
    return name;
  }

  public String values() {
    return "name: " + name + ", templateFile: " + templateFile + ", dataFile: " + dataFile + ", dependencies: " + dependencies;
  }

  public static Project load(Path projectPath) throws IOException {
    Properties props = new Properties();
    try (InputStream in = Files.newInputStream(projectPath)) {
      props.load(in);
    }
    Project p = new Project();
    p.setName(props.getProperty("name"));
    String templateFile = props.getProperty("templateFile");
    if (templateFile == null) {
      Alerts.warn("Problem loading project file", "templateFile for project " + p.getName() + " does not exist");
    } else {
      p.setTemplateFile(absolutePath(Paths.get(templateFile), projectPath));
    }
    String dataFilePref = props.getProperty("dataFile");
    if (dataFilePref != null) {
      p.setDataFile(absolutePath(Paths.get(dataFilePref), projectPath));
    }
    p.setDependencies(props.getProperty("dependencies"), projectPath);
    return p;
  }

  public static void save(Project p, Path projectFilePath) throws IOException {
    Properties props = new Properties();
    if (p.getName() != null) props.setProperty("name", p.getName());
    if (p.getTemplateFile() != null) props.setProperty("templateFile", pathRelativeTo(p.getTemplateFile(), projectFilePath).toString());
    if (p.getDataFile() != null) props.setProperty("dataFile", pathRelativeTo(p.getDataFile(), projectFilePath).toString());
    if (p.getDependencies() != null) props.setProperty("dependencies", p.getDependencyString(projectFilePath));
    try (OutputStream out = Files.newOutputStream(projectFilePath)) {
      if (!Files.exists(projectFilePath.getParent())) {
        Files.createDirectories(projectFilePath.getParent());
      }
      props.store(out, "Journo project file");
    }
  }

  private String getDependencyString(Path projectFilePath) {
    return String.join(";", getDependencies().stream().map(d -> pathRelativeTo(d, projectFilePath).toString()).toList());
  }

  private static Path pathRelativeTo(Path path, Path projectFilePath) {
    Path projectDir = projectFilePath.getParent().toAbsolutePath();
    return projectDir.relativize(path);
  }

  private static Path absolutePath(Path path, Path projectFilePath) {
    if (!Files.isDirectory(projectFilePath)) {
      projectFilePath = projectFilePath.getParent();
    }
    return projectFilePath.resolve(path).normalize();
  }

}
