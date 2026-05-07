# Benchmark Results

| Name | Iterations | Mean (µs) | P50 (µs) | P95 (µs) | Min (µs) | Max (µs) | Notes |
|------|-----------:|----------:|---------:|---------:|---------:|---------:|-------|
| startup-eager | 20 | 1681.27 | 876.12 | 7484.38 | 778.82 | 7484.38 | lazyInit=false (eager pre-instantiation) |
| startup-lazy | 20 | 1355.09 | 553.14 | 6108.37 | 455.14 | 6108.37 | lazyInit=true (deferred singleton) |
| singleton-lookup | 500 | 1.26 | 0.95 | 1.12 | 0.80 | 44.10 | Cached singleton lookup |
