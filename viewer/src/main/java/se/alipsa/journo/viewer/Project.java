package se.alipsa.journo.viewer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Project {

  private String name;
  private String templateFile;
  private String dataFile;

  private String dependencies;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTemplateFile() {
    return templateFile;
  }

  public void setTemplateFile(String templateFile) {
    this.templateFile = templateFile;
  }

  public String getDataFile() {
    return dataFile;
  }

  public void setDataFile(String dataFile) {
    this.dataFile = dataFile;
  }

  public String getDependencies() {
    return dependencies;
  }

  public Collection<String> getDependencyList() {
    List<String> l = new ArrayList<>();
    if (dependencies != null) {
      Collections.addAll(l, dependencies.split(";"));
    }
    return l;
  }

  public void setDependencies(Collection<String> dependencies) {
    this.dependencies = String.join(";", dependencies);
  }
  public void setDependencies(String dependencies) {
    this.dependencies = dependencies;
  }

  @Override
  public String toString() {
    return name;
  }

  public String values() {
    return "name: " + name + ", templateFile: " + templateFile + ", dataFile: " + dataFile + ", dependencies: " + dependencies;
  }

  public static Project load(String projectFile) throws IOException {
    Properties props = new Properties();
    try (InputStream in = Files.newInputStream(Paths.get(projectFile))) {
      props.load(in);
    }
    Project p = new Project();
    p.setName(props.getProperty("name"));
    p.setTemplateFile(props.getProperty("templateFile"));
    p.setDataFile(props.getProperty("dataFile"));
    p.setDependencies(props.getProperty("dependencies"));
    return p;
  }

  public static void save(Project p, String path) throws IOException {
    Properties props = new Properties();
    if (p.getName() != null) props.setProperty("name", p.getName());
    if (p.getTemplateFile() != null) props.setProperty("templateFile", p.getTemplateFile());
    if (p.getDataFile() != null) props.setProperty("dataFile", p.getDataFile());
    if (p.getDependencies() != null) props.setProperty("dependencies", p.getDependencies());
    try (OutputStream out = Files.newOutputStream(Paths.get(path))) {
      props.store(out, "Journo project file");
    }
  }

}
