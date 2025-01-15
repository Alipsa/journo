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
    Path basePath = Paths.get(".").toAbsolutePath();
    Path path = basePath.resolve(Paths.get( "target/JournoTest.prj"));
    Project p = new Project();
    p.setName("Test project");
    p.setTemplateFile(basePath.resolve(Paths.get("target/JournoTest.ftl")));
    p.setDataFile(basePath.resolve(Paths.get("target/JournoTest.groovy")));

    Project.save(p, path);
    //System.out.println(Files.readString(path));

    Project p2 = Project.load(path);
    assertEquals("Test project", p2.getName());
    assertEquals(basePath.resolve(Paths.get("target/JournoTest.ftl")).normalize(), p2.getTemplateFile());
    assertEquals(basePath.resolve(Paths.get("target/JournoTest.groovy")).normalize(), p2.getDataFile());
    assertIterableEquals(Collections.emptyList(), p2.getDependencies());

    Properties props = new Properties();
    try (InputStream is = Files.newInputStream(path)) {
      props.load(is);
    }
    assertEquals("Test project", props.getProperty("name"));
    assertEquals("JournoTest.ftl", props.getProperty("templateFile"));
    assertEquals("JournoTest.groovy", props.getProperty("dataFile"));
  }

  @Test
  public void testStoreAndLoadSpread() throws IOException {
    Path basePath = Paths.get(".").toAbsolutePath();
    Path path = basePath.resolve(Paths.get("target/JournoTest2.prj"));
    Project p = new Project();
    p.setName("Test2 project");
    p.setTemplateFile(basePath.resolve(Paths.get("target/foo/JournoTest.ftl")));
    p.setDataFile(basePath.resolve(Paths.get("target/bar/JournoTest.groovy")));
    p.setDependencies(List.of(basePath.resolve(Paths.get("target/dep/jkl.jar")), basePath.resolve(Paths.get("target/dep/lkn.jar"))));

    Project.save(p, path);
    //System.out.println(Files.readString(path));

    Project p2 = Project.load(path);
    assertEquals("Test2 project", p2.getName());
    assertEquals(basePath.resolve(Paths.get("target/foo/JournoTest.ftl")).normalize(), p2.getTemplateFile());
    assertEquals(basePath.resolve(Paths.get("target/bar/JournoTest.groovy")).normalize(), p2.getDataFile());
    assertEquals(2, p2.getDependencies().size());
    //System.out.println(p2.getDependencies());
    assertIterableEquals(List.of(basePath.resolve(Paths.get("target/dep/jkl.jar")).normalize(), basePath.resolve(Paths.get("target/dep/lkn.jar")).normalize()),
        p2.getDependencies());

    Properties props = new Properties();
    try (InputStream is = Files.newInputStream(path)) {
      props.load(is);
    }
    assertEquals("Test2 project", props.getProperty("name"));
    assertEquals("foo/JournoTest.ftl", props.getProperty("templateFile"));
    assertEquals("bar/JournoTest.groovy", props.getProperty("dataFile"));
    assertEquals("dep/jkl.jar;dep/lkn.jar", props.getProperty("dependencies"));

    //new File("/tmp/JournoTest.prj").deleteOnExit();
  }
}
