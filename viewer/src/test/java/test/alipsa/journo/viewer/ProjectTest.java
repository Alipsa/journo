package test.alipsa.journo.viewer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import se.alipsa.journo.viewer.Project;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class ProjectTest {

  @Test
  public void testStoreAndLoad() throws IOException {
    Path path = Paths.get("/tmp/JournoTest.prj");
    Project p = new Project();
    p.setName("Test project");
    p.setTemplateFile(Paths.get("/tmp/JournoTest.ftl"));
    p.setDataFile(Paths.get("/tmp/JournoTest.groovy"));

    Project.save(p, path);
    System.out.println(Files.readString(path));

    Project p2 = Project.load(path);
    assertEquals("Test project", p2.getName());
    assertEquals("/tmp/JournoTest.ftl", p2.getTemplateFile().toString());
    assertEquals("/tmp/JournoTest.groovy", p2.getDataFile().toString());
    assertIterableEquals(Collections.emptyList(), p2.getDependencies());

    Properties props = new Properties();
    try (InputStream is = Files.newInputStream(path)) {
      props.load(is);
    }
    assertEquals("Test project", props.getProperty("name"));
    assertEquals("JournoTest.ftl", props.getProperty("templateFile"));
    assertEquals("JournoTest.groovy", props.getProperty("dataFile"));

    new File("/tmp/JournoTest.prj").deleteOnExit();
  }

  @Test
  public void testStoreAndLoadSpread() throws IOException {
    Path path = Paths.get("/tmp/JournoTest2.prj");
    Project p = new Project();
    p.setName("Test2 project");
    p.setTemplateFile(Paths.get("/tmp/foo/JournoTest.ftl"));
    p.setDataFile(Paths.get("/tmp/bar/JournoTest.groovy"));
    p.setDependencies(List.of(Paths.get("/tmp/dep/jkl.jar"), Paths.get("/tmp/dep/lkn.jar")));

    Project.save(p, path);
    System.out.println(Files.readString(path));

    Project p2 = Project.load(path);
    assertEquals("Test2 project", p2.getName());
    assertEquals("/tmp/foo/JournoTest.ftl", p2.getTemplateFile().toString());
    assertEquals("/tmp/bar/JournoTest.groovy", p2.getDataFile().toString());
    assertEquals(2, p2.getDependencies().size());
    assertIterableEquals(List.of(Paths.get("/tmp/dep/jkl.jar"), Paths.get("/tmp/dep/lkn.jar")),
        p2.getDependencies());

    Properties props = new Properties();
    try (InputStream is = Files.newInputStream(path)) {
      props.load(is);
    }
    assertEquals("Test2 project", props.getProperty("name"));
    assertEquals("foo/JournoTest.ftl", props.getProperty("templateFile"));
    assertEquals("bar/JournoTest.groovy", props.getProperty("dataFile"));
    assertEquals("dep/jkl.jar;dep/lkn.jar", props.getProperty("dependencies"));

    new File("/tmp/JournoTest.prj").deleteOnExit();
  }
}
