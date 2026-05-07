package mn.edu.num.benchmark;

import java.util.List;

/**
 * Benchmark үр дүнг JSON эсвэл Markdown форматаар үзүүлэх туслах класс.
 */
public final class BenchmarkReport {

    private BenchmarkReport() {}

    public static String toJson(List<BenchmarkResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < results.size(); i++) {
            BenchmarkResult r = results.get(i);
            sb.append("  {\n");
            sb.append("    \"name\": \"").append(escape(r.name())).append("\",\n");
            sb.append("    \"iterations\": ").append(r.iterations()).append(",\n");
            sb.append("    \"meanNanos\": ").append(r.meanNanos()).append(",\n");
            sb.append("    \"p50Nanos\": ").append(r.p50Nanos()).append(",\n");
            sb.append("    \"p95Nanos\": ").append(r.p95Nanos()).append(",\n");
            sb.append("    \"minNanos\": ").append(r.minNanos()).append(",\n");
            sb.append("    \"maxNanos\": ").append(r.maxNanos()).append(",\n");
            sb.append("    \"notes\": \"").append(escape(r.notes())).append("\"\n");
            sb.append("  }").append(i < results.size() - 1 ? "," : "").append("\n");
        }
        sb.append("]\n");
        return sb.toString();
    }

    public static String toMarkdown(List<BenchmarkResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Benchmark Results\n\n");
        sb.append("| Name | Iterations | Mean (µs) | P50 (µs) | P95 (µs) | Min (µs) | Max (µs) | Notes |\n");
        sb.append("|------|-----------:|----------:|---------:|---------:|---------:|---------:|-------|\n");
        for (BenchmarkResult r : results) {
            sb.append("| ").append(r.name())
                    .append(" | ").append(r.iterations())
                    .append(" | ").append(formatMicros(r.meanNanos()))
                    .append(" | ").append(formatMicros(r.p50Nanos()))
                    .append(" | ").append(formatMicros(r.p95Nanos()))
                    .append(" | ").append(formatMicros(r.minNanos()))
                    .append(" | ").append(formatMicros(r.maxNanos()))
                    .append(" | ").append(r.notes()).append(" |\n");
        }
        return sb.toString();
    }

    private static String formatMicros(long nanos) {
        return String.format("%.2f", nanos / 1_000.0);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
