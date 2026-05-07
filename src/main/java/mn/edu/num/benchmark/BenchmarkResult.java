package mn.edu.num.benchmark;

/**
 * Нэг benchmark туршилтын нэгдсэн үр дүнг агуулна.
 *
 * @param name        Туршилтын нэр (жишээ нь, "startup-100-beans")
 * @param iterations  Хэдэн удаа давтсан
 * @param meanNanos   Дунд хугацаа (ns)
 * @param p50Nanos    Median (ns)
 * @param p95Nanos    P95 (ns)
 * @param minNanos    Хамгийн бага хугацаа (ns)
 * @param maxNanos    Хамгийн их хугацаа (ns)
 * @param notes       Нэмэлт тайлбар (жишээ нь, "with reflection caching")
 */
public record BenchmarkResult(
        String name,
        int iterations,
        long meanNanos,
        long p50Nanos,
        long p95Nanos,
        long minNanos,
        long maxNanos,
        String notes
) {
    public double meanMicros() {
        return meanNanos / 1_000.0;
    }

    public double meanMillis() {
        return meanNanos / 1_000_000.0;
    }
}
