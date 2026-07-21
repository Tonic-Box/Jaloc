# Benchmarks

All numbers below are from a single-threaded run of 1,000,000 elements per test, taking the best of 5 timed passes after warm-up.
Ratio is how many times faster Jaloc is. Anything above 1.00x means Jaloc wins that row.

## Jaloc vs Java built-ins

| Benchmark | Jaloc | JDK | Ratio |
|---|---:|---:|---:|
| list sort (vs ArrayList) | 7.21 ms | 296.00 ms | **41.03x** |
| short set add (bitmap) | 1.53 ms | 35.49 ms | **23.24x** |
| heap push+pop 500k | 30.47 ms | 271.56 ms | **8.91x** |
| int array sort | 6.68 ms | 48.20 ms | **7.22x** |
| bitset range set | 0.01 ms | 0.08 ms | **7.18x** |
| float array sort | 9.90 ms | 53.04 ms | **5.35x** |
| list add | 0.99 ms | 5.19 ms | **5.26x** |
| stack push+pop | 1.49 ms | 6.77 ms | **4.53x** |
| queue cycle | 2.08 ms | 6.91 ms | **3.33x** |
| list get-sum | 0.54 ms | 1.67 ms | **3.11x** |
| deque mixed ends | 2.85 ms | 8.48 ms | **2.97x** |
| int set add | 26.60 ms | 72.82 ms | **2.74x** |
| double array sort | 20.88 ms | 53.39 ms | **2.56x** |
| list bulk load (addAll) | 0.90 ms | 1.99 ms | **2.20x** |
| int set contains | 18.14 ms | 32.22 ms | **1.78x** |
| binary search 512k x1M | 67.45 ms | 92.62 ms | **1.37x** |
| map put (shuffled) | 44.38 ms | 59.83 ms | **1.35x** |
| bool list add+read | 4.07 ms | 5.30 ms | **1.30x** |
| struct field iteration | 1.81 ms | 2.07 ms | **1.15x** |
| bitset set 1M bits | 0.90 ms | 0.94 ms | **1.04x** |
| map get (shuffled) | 32.51 ms | 33.41 ms | **1.03x** |
| bitset iterate set bits | 0.15 ms | 0.09 ms | 0.59x |

**Summary:** Jaloc wins 21 of 22 rows, largely by avoiding boxing. The only loss is dense bitset iteration, by a fraction of a millisecond. Jaloc also allocates no garbage in any row; the JDK equivalents box millions of objects, and that GC cost is not reflected in these timings.

## Jaloc vs fastutil

[fastutil](https://fastutil.di.unimi.it/) stores data in on-heap Java arrays; Jaloc stores data in native memory off the heap.

| Benchmark | Jaloc | fastutil | Ratio |
|---|---:|---:|---:|
| float array sort | 9.86 ms | 39.52 ms | **4.01x** |
| int array sort | 6.52 ms | 24.05 ms | **3.69x** |
| list sort | 7.15 ms | 24.15 ms | **3.38x** |
| queue cycle | 1.98 ms | 5.69 ms | **2.87x** |
| short set add | 1.56 ms | 4.20 ms | **2.69x** |
| deque mixed ends | 2.43 ms | 6.02 ms | **2.48x** |
| double array sort | 21.64 ms | 37.73 ms | **1.74x** |
| heap push+pop 500k | 27.92 ms | 40.40 ms | **1.45x** |
| binary search 512k x1M | 68.53 ms | 90.68 ms | **1.32x** |
| map get (shuffled) | 28.91 ms | 26.38 ms | 0.91x |
| list bulk load | 0.44 ms | 0.37 ms | 0.83x |
| int set contains | 15.20 ms | 12.67 ms | 0.83x |
| list add | 0.86 ms | 0.64 ms | 0.74x |
| list get-sum | 0.38 ms | 0.28 ms | 0.74x |
| map put (shuffled) | 39.65 ms | 29.16 ms | 0.74x |
| int set add | 21.12 ms | 15.25 ms | 0.72x |
| stack push+pop | 1.19 ms | 0.85 ms | 0.71x |
| bool list add+read | 4.06 ms | 1.00 ms | 0.25x |

*(bitset and struct benchmarks omitted - fastutil has no equivalents)*

**Summary:** algorithm-heavy operations - sorting, searching, heaps, queues - go to Jaloc by 1.3x to 4x. Plain per-element loops go to fastutil by 20-30%, since the JVM optimizes on-heap arrays harder than native memory access. The bool list gap is a layout trade: Jaloc bit-packs at 1/8th the memory, fastutil spends a byte per value for speed.

Beyond the timings, Jaloc adds nothing to GC pauses, holds collections past 2 billion elements, frees memory on `close()`, and supports struct layouts - none of which fastutil offers.