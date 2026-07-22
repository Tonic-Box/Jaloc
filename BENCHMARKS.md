# Benchmarks

All numbers below are from single-threaded runs of 1,000,000 elements per test, taking the best of 5 timed passes after warm-up; each row is the median of three full runs.
Ratio is how many times faster Jaloc is. Anything above 1.00x means Jaloc wins that row.

## Jaloc vs Java built-ins

| Benchmark | Jaloc | JDK | Ratio |
|---|---:|---:|---:|
| list sort (vs ArrayList) | 6.42 ms | 287.09 ms | **44.69x** |
| short set add (bitmap) | 1.53 ms | 29.08 ms | **18.96x** |
| heap push+pop 500k | 26.56 ms | 215.53 ms | **8.11x** |
| bitset range set | 0.01 ms | 0.08 ms | **7.71x** |
| int array sort | 6.72 ms | 49.40 ms | **7.35x** |
| list add | 0.87 ms | 5.17 ms | **5.93x** |
| stack push+pop | 1.18 ms | 6.20 ms | **5.26x** |
| float array sort | 9.87 ms | 51.85 ms | **5.25x** |
| list bulk load (addAll) | 0.53 ms | 1.82 ms | **3.45x** |
| queue cycle | 1.97 ms | 6.15 ms | **3.12x** |
| deque mixed ends | 2.43 ms | 7.22 ms | **2.97x** |
| int set add | 22.68 ms | 62.46 ms | **2.75x** |
| int set contains | 14.33 ms | 38.64 ms | **2.70x** |
| list get-sum | 0.43 ms | 1.14 ms | **2.67x** |
| double array sort | 21.65 ms | 52.53 ms | **2.42x** |
| binary search 512k x1M | 66.13 ms | 92.31 ms | **1.40x** |
| map put (shuffled) | 39.30 ms | 48.88 ms | **1.24x** |
| struct field iteration | 1.63 ms | 1.81 ms | **1.11x** |
| bitset set 1M bits | 0.88 ms | 0.97 ms | **1.10x** |
| bool list add+read | 6.18 ms | 6.79 ms | **1.10x** |
| map get (shuffled) | 30.69 ms | 30.82 ms | 1.00x |
| bitset iterate set bits | 0.13 ms | 0.08 ms | 0.66x |

**Summary:** Jaloc wins 20 of 22 rows and ties one, largely by avoiding boxing. The only loss is dense bitset iteration, by a fraction of a millisecond. Jaloc also allocates no garbage in any row; the JDK equivalents box millions of objects, and that GC cost is not reflected in these timings.

## Jaloc vs fastutil

[fastutil](https://fastutil.di.unimi.it/) stores data in on-heap Java arrays; Jaloc stores data in native memory off the heap.

| Benchmark | Jaloc | fastutil | Ratio |
|---|---:|---:|---:|
| float array sort | 9.58 ms | 38.41 ms | **4.01x** |
| list sort | 6.36 ms | 22.51 ms | **3.54x** |
| int array sort | 6.61 ms | 22.97 ms | **3.48x** |
| queue cycle | 1.98 ms | 5.94 ms | **3.01x** |
| short set add | 1.55 ms | 4.06 ms | **2.62x** |
| deque mixed ends | 2.50 ms | 5.72 ms | **2.28x** |
| double array sort | 22.14 ms | 39.08 ms | **1.76x** |
| heap push+pop 500k | 25.73 ms | 41.09 ms | **1.60x** |
| binary search 512k x1M | 70.90 ms | 90.03 ms | **1.27x** |
| map get (shuffled) | 27.15 ms | 25.03 ms | 0.92x |
| stack push+pop | 1.17 ms | 1.01 ms | 0.86x |
| int set contains | 16.11 ms | 13.47 ms | 0.84x |
| list bulk load | 0.48 ms | 0.36 ms | 0.75x |
| list add | 0.93 ms | 0.68 ms | 0.73x |
| int set add | 22.39 ms | 16.08 ms | 0.72x |
| map put (shuffled) | 39.90 ms | 28.29 ms | 0.71x |
| list get-sum | 0.42 ms | 0.27 ms | 0.65x |
| bool list add+read | 4.00 ms | 1.02 ms | 0.25x |

*(bitset and struct benchmarks omitted - fastutil has no equivalents)*

**Summary:** algorithm-heavy operations - sorting, searching, heaps, queues - go to Jaloc by 1.3x to 4x. Plain per-element loops go to fastutil by 15-35%, since the JVM optimizes on-heap arrays harder than native memory access. The bool list gap is a layout trade: Jaloc bit-packs at 1/8th the memory, fastutil spends a byte per value for speed.

Beyond the timings, Jaloc adds nothing to GC pauses, holds collections past 2 billion elements, frees memory on `close()`, and supports struct layouts - none of which fastutil offers.

## Spill mode (memory-mapped) vs malloc

`MappedAllocator.ephemeral()` backs collections with memory-mapped temp files the kernel deletes at process exit. Pages become OS-reclaimable under memory pressure instead of pinned like malloc. Same collections, same code - only the allocator changes.

| Benchmark | malloc | spill | Ratio |
|---|---:|---:|---:|
| long set contains 1M | 38.20 ms | 38.16 ms | **1.00x** |
| long list get-sum 1M | 0.88 ms | 0.86 ms | **1.02x** |
| long set add 1M | 42.17 ms | 69.22 ms | 0.61x |
| long list add 1M | 7.53 ms | 44.69 ms | 0.17x |

**Summary:** once pages are resident, reads run at full malloc speed - the CPU does not know the page is file-backed. Building a collection is slower: each growth step maps a fresh file and every first page touch is a soft fault, costs malloc does not pay. Spill mode is for large, read-heavy, or memory-pressured workloads, not write throughput.

## Persistent arrays vs malloc

`PLongArray.create(path, length)` and `PLongArray.open(path)` back a fixed array with a named file that outlives the process. File length is the array's complete state, so no header or metadata is stored.

| Benchmark | malloc | mapped file | Ratio |
|---|---:|---:|---:|
| read-sum 1M longs | 0.70 ms | 0.81 ms | 0.87x |
| fill 1M longs | 0.70 ms | 2.77 ms | 0.25x |

First fill of a fresh 8 MB file costs 7.7-8.7 ms including page faults; opening and mapping that file afterwards takes 0.6 ms.

**Summary:** reads are near parity once resident. Writes pay for dirty-page tracking and kernel writeback, which is the price of the data still being there after the process exits - verified surviving both a clean close and a hard `halt()` with no close at all.

## Reproducing

Each row runs a Jaloc collection against its JDK or fastutil equivalent on identical random data, verified for matching results before timing. Numbers vary by machine; treat the ratios as the takeaway, not the absolute times.
