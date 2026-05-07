package mn.edu.num.unit;

import mn.edu.num.benchmark.BenchmarkReport;
import mn.edu.num.benchmark.BenchmarkResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BenchmarkReportTest {

    @Test
    void jsonShouldContainAllFields() {
        BenchmarkResult r = new BenchmarkResult("startup", 100, 1000, 800, 1500, 700, 2000, "test");
        String json = BenchmarkReport.toJson(List.of(r));
        assertTrue(json.contains("\"name\": \"startup\""));
        assertTrue(json.contains("\"iterations\": 100"));
        assertTrue(json.contains("\"meanNanos\": 1000"));
    }

    @Test
    void markdownShouldRenderTable() {
        BenchmarkResult r = new BenchmarkResult("startup", 100, 1000, 800, 1500, 700, 2000, "n");
        String md = BenchmarkReport.toMarkdown(List.of(r));
        assertTrue(md.contains("| Name |"));
        assertTrue(md.contains("startup"));
    }
}
