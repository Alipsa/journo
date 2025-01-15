package test.alipsa.journo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.openhtmltopdf.util.XRLog;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import se.alipsa.journo.JournoEngine;
import se.alipsa.journo.JournoException;

public class ConcurrencyTest {

  static {
    // silence openhtmltopdf noice
    XRLog.setLevel(XRLog.LOAD, Level.WARNING);
    XRLog.setLevel(XRLog.MATCH, Level.WARNING);
    XRLog.setLevel(XRLog.GENERAL, Level.WARNING);
  }

  Map<String, Object> data1 = Map.of("svgImage", """
            <h2>A big blue circle</h2>
            <svg xmlns="http://www.w3.org/2000/svg">
                <circle cx="150" cy="90" r="80" stroke="black" stroke-width="3"
                        fill="blue" />
            </svg>
        """);

  Map<String, Object> data2 = Map.of("svgImage", """
            <h2>A big yellow circle</h2>
            <svg xmlns="http://www.w3.org/2000/svg">
                <circle cx="150" cy="90" r="80" stroke="blue" stroke-width="3"
                        fill="yellow" />
            </svg>
        """);

  @Test
  public void testSameInstanceConcurrently() throws InterruptedException, IOException {
    JournoEngine engine = new JournoEngine(this, "/templates");

    Path path1 = Paths.get("target/blue.pdf");
    Path path2 = Paths.get("target/yellow.pdf");

    ExecutorService executorService = Executors.newFixedThreadPool(2);
    Runnable task1 = () -> {
      //System.out.println("Task 1 in testSameInstanceConcurrently is executing...");
      try {
        engine.renderPdf("svgImage.ftlh", data1, path1);
      } catch (JournoException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      //System.out.println("Task 1 in testSameInstanceConcurrently is finished!");
    };
    Runnable task2 = () -> {
      //System.out.println("Task 2 in testSameInstanceConcurrently is executing...");
      try {
        engine.renderPdf("svgImage.ftlh", data2, path2);
      } catch (JournoException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      //System.out.println("Task 2 in testSameInstanceConcurrently is finished!");
    };
    executorService.submit(task1);
    executorService.submit(task2);
    executorService.shutdown();
    assertTrue(executorService.awaitTermination(3, TimeUnit.SECONDS));
    assertTrue(path1.toFile().exists());
    assertTrue(path2.toFile().exists());
    assertTrue(extractContent(path1).contains("A big blue circle"));
    assertTrue(extractContent(path2).contains("A big yellow circle"));
  }

  @Test
  public void testDifferentInstancesConcurrently() throws InterruptedException, IOException {
    Path path1 = Paths.get("target/blue2.pdf");
    Path path2 = Paths.get("target/yellow2.pdf");

    ExecutorService executorService = Executors.newFixedThreadPool(2);
    Runnable task1 = () -> {
      //System.out.println("Task 1 in testDifferentInstancesConcurrently is executing...");
      JournoEngine engine = new JournoEngine(this, "/templates");
      try {
        engine.renderPdf("svgImage.ftlh", data1, path1);
      } catch (JournoException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      //System.out.println("Task 1 in testDifferentInstancesConcurrently is finished!");
    };
    Runnable task2 = () -> {
      //System.out.println("Task 2 in testDifferentInstancesConcurrently is executing...");
      JournoEngine engine = new JournoEngine(this, "/templates");
      try {
        engine.renderPdf("svgImage.ftlh", data2, path2);
      } catch (JournoException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      //System.out.println("Task 2 in testDifferentInstancesConcurrently is finished!");
    };
    executorService.submit(task1);
    executorService.submit(task2);
    executorService.shutdown();
    assertTrue(executorService.awaitTermination(3, TimeUnit.SECONDS));
    assertTrue(path1.toFile().exists());
    assertTrue(path2.toFile().exists());
    assertTrue(extractContent(path1).contains("A big blue circle"));
    assertTrue(extractContent(path2).contains("A big yellow circle"));
  }

  String extractContent(Path path) throws IOException {
    try(PDDocument pdf = Loader.loadPDF(path.toFile())) {
      return new PDFTextStripper().getText(pdf);
    }
  }
}
