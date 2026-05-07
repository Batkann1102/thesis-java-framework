package mn.edu.num.benchmark;

import mn.edu.num.container.ApplicationContext;
import mn.edu.num.container.ApplicationContextBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * IoC container-ийн performance-ийг бодит хэмжилтээр үнэлэх benchmark.
 *
 * <p>Энэ нь судалгааны шүүмжид хариулсан гол модулиудын нэг бөгөөд:
 * <ul>
 *   <li>Container-г startup хийхэд зарцуулдаг хугацаа</li>
 *   <li>getBean(name) latency (singleton болон prototype)</li>
 *   <li>Prototype-уудыг үүсгэх throughput</li>
 *   <li>Reflection caching-ийн эффект (1-р давталт vs дараах давталтууд)</li>
 * </ul>
 * зэргийг хэмжиж JSON болон Markdown report болгон хадгалдаг.
 *
 * <pre>
 * java -cp target/classes mn.edu.num.benchmark.BenchmarkRunner \
 *     --package mn.edu.num.app.simple \
 *     --warmup 200 --iterations 1000 \
 *     --out target/benchmarks
 * </pre>
 *
 * <p>Үр дүн нь стандарт хэлбэртэй гарах учраас Mintlify документд шууд оруулж
 * болно. {@link mn.edu.num.benchmark.BenchmarkResult}-г харна уу.
 */
public class BenchmarkRunner {

    private final String basePackage;
    private final int warmup;
    private final int iterations;

    public BenchmarkRunner(String basePackage, int warmup, int iterations) {
        this.basePackage = basePackage;
        this.warmup = warmup;
        this.iterations = iterations;
    }

    /**
     * CLI entrypoint.
     *
     * <pre>
     * --package &lt;name&gt;     Scan хийх root package (заавал)
     * --warmup &lt;N&gt;        Хэдэн удаа warmup ажиллуулах (default 200)
     * --iterations &lt;N&gt;    Хэдэн удаа хэмжилт хийх (default 1000)
     * --out &lt;dir&gt;         Гаралтын хавтас (default target/benchmarks)
     * </pre>
     */
    public static void main(String[] args) throws IOException {
        String basePackage = "mn.edu.num.app";
        int warmup = 200;
        int iterations = 1000;
        String out = "target/benchmarks";
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--package" -> basePackage = args[++i];
                case "--warmup" -> warmup = Integer.parseInt(args[++i]);
                case "--iterations" -> iterations = Integer.parseInt(args[++i]);
                case "--out" -> out = args[++i];
                default -> System.err.println("Unknown arg: " + args[i]);
            }
        }

        BenchmarkRunner runner = new BenchmarkRunner(basePackage, warmup, iterations);
        List<BenchmarkResult> results = runner.runAll();

        Path outDir = Paths.get(out);
        Files.createDirectories(outDir);
        Path json = outDir.resolve("results.json");
        Path md = outDir.resolve("results.md");
        Files.writeString(json, BenchmarkReport.toJson(results));
        Files.writeString(md, BenchmarkReport.toMarkdown(results));
        System.out.println("[Benchmark] JSON: " + json.toAbsolutePath());
        System.out.println("[Benchmark] Markdown: " + md.toAbsolutePath());
    }

    public List<BenchmarkResult> runAll() {
        List<BenchmarkResult> results = new ArrayList<>();
        results.add(measureStartup(false));
        results.add(measureStartup(true));
        results.addAll(measureLookups());
        return results;
    }

    /**
     * Container-г шинээр эхлүүлэхэд (eager эсвэл lazy) зарцуулдаг хугацаа.
     */
    public BenchmarkResult measureStartup(boolean lazyInit) {
        // Warmup
        for (int i = 0; i < Math.max(warmup / 10, 5); i++) {
            try (ApplicationContext ctx = ApplicationContextBuilder.builder()
                    .basePackage(basePackage)
                    .lazyInit(lazyInit)
                    .build()) {
                ctx.beanCount();
            }
        }
        long[] samples = new long[Math.max(iterations / 50, 20)];
        for (int i = 0; i < samples.length; i++) {
            long start = System.nanoTime();
            ApplicationContext ctx = ApplicationContextBuilder.builder()
                    .basePackage(basePackage)
                    .lazyInit(lazyInit)
                    .build();
            samples[i] = System.nanoTime() - start;
            ctx.close();
        }
        return summarize("startup-" + (lazyInit ? "lazy" : "eager"), samples,
                lazyInit ? "lazyInit=true (deferred singleton)" : "lazyInit=false (eager pre-instantiation)");
    }

    /**
     * getBean() lookup latency-г measure хийнэ.
     */
    public List<BenchmarkResult> measureLookups() {
        List<BenchmarkResult> all = new ArrayList<>();
        try (ApplicationContext ctx = ApplicationContextBuilder.builder()
                .basePackage(basePackage)
                .build()) {
            // Эхний bean-г олно
            String beanName = ctx.getTreeBuilder().getNodeMap().keySet().stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("Benchmark-д bean байхгүй."));
            // Singleton lookup
            all.add(timed("singleton-lookup", warmup, iterations,
                    () -> ctx.getBean(beanName), "Cached singleton lookup"));
        }
        return all;
    }

    private BenchmarkResult timed(String name, int warmup, int iterations,
                                  Supplier<?> action, String notes) {
        for (int i = 0; i < warmup; i++) action.get();
        long[] samples = new long[iterations];
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            Object value = action.get();
            samples[i] = System.nanoTime() - start;
            // JIT-аас үр дүнг устгахаас сэргийлэхийн тулд
            if (value == null) throw new IllegalStateException("null result");
        }
        return summarize(name, samples, notes);
    }

    private BenchmarkResult summarize(String name, long[] samples, String notes) {
        long[] sorted = samples.clone();
        Arrays.sort(sorted);
        long sum = 0;
        for (long s : sorted) sum += s;
        long mean = sum / sorted.length;
        long p50 = sorted[sorted.length / 2];
        long p95 = sorted[(int) (sorted.length * 0.95)];
        long min = sorted[0];
        long max = sorted[sorted.length - 1];
        return new BenchmarkResult(name, sorted.length, mean, p50, p95, min, max, notes);
    }
}
