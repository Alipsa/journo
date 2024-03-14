package test.alipsa.journo.viewer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import se.alipsa.journo.viewer.Project;

import java.io.File;
import java.io.IOException;

public class ProjectTest {

  @Test
  public void testStoreAndLoad() throws IOException {
    Project p = new Project();
    p.setName("Test project");
    p.setTemplateFile("/tmp/JournoTest.ftl");
    p.setDataFile("/tmp/JournoTest.groovy");

    Project.save(p, "/tmp/JournoTest.prj");

    Project p2 = Project.load("/tmp/JournoTest.prj");
    assertEquals("Test project", p2.getName());
    assertEquals("/tmp/JournoTest.ftl", p2.getTemplateFile());
    assertEquals("/tmp/JournoTest.groovy", p2.getDataFile());

    new File("/tmp/JournoTest.prj").delete();
  }
}
