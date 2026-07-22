package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.FileMappedAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

import java.nio.file.Path;

/**
 * A fixed-length native float array.
 */
public final class PFloatArray extends AbstractPrimitiveArray<PFloatWriter>
{
    /**
     * Allocates length elements on the system allocator, zeroed.
     *
     * @param length the element count
     * @throws IllegalArgumentException if length is negative
     */
    public PFloatArray(long length)
    {
        super(ElementSize.DWORD, length);
    }

    /**
     * Allocates length elements on the given allocator, zeroed.
     *
     * @param allocator the allocator to source memory from
     * @param length the element count
     * @throws IllegalArgumentException if length is negative
     */
    public PFloatArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.DWORD, length);
    }

    /**
     * Creates a new file holding length elements and maps it, zeroed.
     *
     * @param path the file to create
     * @param length the element count
     * @return the array over the mapped file
     * @throws IllegalArgumentException if length is not positive
     * @throws java.io.UncheckedIOException if the file already exists or cannot be created
     * @throws UnsupportedOperationException if file mapping is unavailable on this JVM
     */
    public static PFloatArray create(Path path, long length)
    {
        return new PFloatArray(FileMappedAllocator.create(path, ElementSize.DWORD.byteSize(MappedArrays.requireLength(length))), length);
    }

    /**
     * Opens an existing file and maps it, keeping its contents.
     *
     * @param path the file to open
     * @return the array over the mapped file
     * @throws IllegalArgumentException if the file is empty or its length is not a multiple of the element width
     * @throws java.io.UncheckedIOException if the file cannot be opened
     * @throws UnsupportedOperationException if file mapping is unavailable on this JVM
     */
    public static PFloatArray open(Path path)
    {
        FileMappedAllocator allocator = FileMappedAllocator.open(path);
        return new PFloatArray(allocator, MappedArrays.elementCount(allocator.fileBytes(), ElementSize.DWORD.getSize()));
    }

    /**
     * Reads the element at index.
     *
     * @param index the element index
     * @return the element
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException if closed
     */
    public float get(long index)
    {
        return readFloat(index);
    }

    /**
     * Writes the element at index.
     *
     * @param index the element index
     * @param value the value to store
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException if closed
     */
    public void set(long index, float value)
    {
        writeFloat(index, value);
    }

    /**
     * Reads the element at index with no liveness or bounds check; the caller must have proven both.
     *
     * @param index the element index
     * @return the element
     */
    public float getUnchecked(long index)
    {
        return readFloatUnchecked(index);
    }

    /**
     * Writes the element at index with no liveness or bounds check; the caller must have proven both.
     *
     * @param index the element index
     * @param value the value to store
     */
    public void setUnchecked(long index, float value)
    {
        writeFloatUnchecked(index, value);
    }

    @Override
    public PFloatWriter writer()
    {
        return new PFloatWriter(this);
    }

    /**
     * Sorts the whole array ascending.
     *
     * @throws IllegalStateException if closed
     */
    public void sort()
    {
        sort(0, length());
    }

    /**
     * Sorts fromIndex inclusive to toIndex exclusive, ascending.
     *
     * @param fromIndex the range start, inclusive
     * @param toIndex the range end, exclusive
     * @throws IndexOutOfBoundsException if the range is out of bounds
     * @throws IllegalStateException if closed
     */
    public void sort(long fromIndex, long toIndex)
    {
        checkRange(fromIndex, toIndex);

        if (toIndex - fromIndex < 2048)
        {
            quicksort(fromIndex, toIndex - 1);
            return;
        }

        long limit = partitionNaN(fromIndex, toIndex);

        if (limit - fromIndex < 2048)
        {
            quicksort(fromIndex, limit - 1);
            return;
        }

        radixSort(fromIndex, limit);
    }

    private long partitionNaN(long fromIndex, long toIndex)
    {
        long limit = toIndex;
        long i = fromIndex;

        while (i < limit)
        {
            float value = getUnchecked(i);

            if (value != value)
            {
                limit--;
                setUnchecked(i, getUnchecked(limit));
                setUnchecked(limit, value);
            }
            else
            {
                i++;
            }
        }

        return limit;
    }

    private void radixSort(long fromIndex, long toIndex)
    {
        long length = toIndex - fromIndex;
        long source = baseAddress() + fromIndex * Float.BYTES;
        long scratch = UnsafeMemory.allocate(length * Float.BYTES);

        try
        {
            long[] counts = new long[4 * 256];

            for (long i = 0; i < length; i++)
            {
                int bits = UnsafeMemory.getInt(source + i * Float.BYTES);
                int value = bits ^ ((bits >> 31) | 0x80000000);

                UnsafeMemory.putInt(source + i * Float.BYTES, value);
                counts[value & 0xFF]++;
                counts[256 + ((value >>> 8) & 0xFF)]++;
                counts[512 + ((value >>> 16) & 0xFF)]++;
                counts[768 + ((value >>> 24) & 0xFF)]++;
            }

            long from = source;
            long to = scratch;

            for (int pass = 0; pass < 4; pass++)
            {
                int bucketBase = pass << 8;

                if (SortSupport.trivialPass(counts, bucketBase, length))
                {
                    continue;
                }

                long[] offsets = new long[256];
                long sum = 0;

                for (int bucket = 0; bucket < 256; bucket++)
                {
                    offsets[bucket] = sum;
                    sum += counts[bucketBase + bucket];
                }

                int shift = pass << 3;

                for (long i = 0; i < length; i++)
                {
                    int value = UnsafeMemory.getInt(from + i * Float.BYTES);
                    int digit = (value >>> shift) & 0xFF;

                    UnsafeMemory.putInt(to + offsets[digit] * Float.BYTES, value);
                    offsets[digit]++;
                }

                long swap = from;

                from = to;
                to = swap;
            }

            for (long i = 0; i < length; i++)
            {
                int value = UnsafeMemory.getInt(from + i * Float.BYTES);

                UnsafeMemory.putInt(source + i * Float.BYTES, value ^ ((~value >> 31) | 0x80000000));
            }
        }
        finally
        {
            UnsafeMemory.free(scratch);
        }
    }

    /**
     * Binary searches the whole array; content must be sorted.
     *
     * @param value the value to find
     * @return the matching index, or -(insertionPoint + 1) if absent
     * @throws IllegalStateException if closed
     */
    public long binarySearch(float value)
    {
        return binarySearch(0, length(), value);
    }

    /**
     * Binary searches fromIndex inclusive to toIndex exclusive; content must be sorted.
     *
     * @param fromIndex the range start, inclusive
     * @param toIndex the range end, exclusive
     * @param value the value to find
     * @return the matching index, or -(insertionPoint + 1) if absent
     * @throws IndexOutOfBoundsException if the range is out of bounds
     * @throws IllegalStateException if closed
     */
    public long binarySearch(long fromIndex, long toIndex, float value)
    {
        checkRange(fromIndex, toIndex);

        if (toIndex - fromIndex <= 524288)
        {
            return branchlessSearch(fromIndex, toIndex, value);
        }

        long low = fromIndex;
        long high = toIndex - 1;

        while (low <= high)
        {
            long mid = low + ((high - low) >>> 1);
            int comparison = Float.compare(getUnchecked(mid), value);

            if (comparison < 0)
            {
                low = mid + 1;
            }
            else if (comparison > 0)
            {
                high = mid - 1;
            }
            else
            {
                return mid;
            }
        }

        return -(low + 1);
    }

    private long branchlessSearch(long fromIndex, long toIndex, float value)
    {
        long base = fromIndex;
        long n = toIndex - fromIndex;

        while (n > 1)
        {
            long half = n >>> 1;

            base = Float.compare(getUnchecked(base + half - 1), value) < 0 ? base + half : base;
            n -= half;
        }

        if (n == 1)
        {
            int comparison = Float.compare(getUnchecked(base), value);

            if (comparison == 0)
            {
                return base;
            }

            if (comparison < 0)
            {
                return -(base + 2);
            }
        }

        return -(base + 1);
    }

    /**
     * Bulk copies length elements from source into this array.
     *
     * @param source the heap array to read from
     * @param sourceIndex the start within source
     * @param destinationIndex the start within this array
     * @param length the element count
     * @throws NullPointerException if source is null
     * @throws IndexOutOfBoundsException if either range is out of bounds
     * @throws IllegalStateException if closed
     */
    public void copyFrom(float[] source, int sourceIndex, long destinationIndex, int length)
    {
        if (source == null)
        {
            throw new NullPointerException("source");
        }

        if (sourceIndex < 0 || length < 0 || sourceIndex > source.length - length)
        {
            throw new IndexOutOfBoundsException("sourceIndex=" + sourceIndex + ", length=" + length + ", sourceLength=" + source.length);
        }

        checkRange(destinationIndex, destinationIndex + length);
        UnsafeMemory.copyFromHeap(source, UnsafeMemory.FLOAT_ARRAY_BASE + (long) sourceIndex * Float.BYTES, baseAddress() + destinationIndex * Float.BYTES, (long) length * Float.BYTES);
    }

    /**
     * Bulk copies length elements from this array into destination.
     *
     * @param sourceIndex the start within this array
     * @param destination the heap array to write into
     * @param destinationIndex the start within destination
     * @param length the element count
     * @throws NullPointerException if destination is null
     * @throws IndexOutOfBoundsException if either range is out of bounds
     * @throws IllegalStateException if closed
     */
    public void copyTo(long sourceIndex, float[] destination, int destinationIndex, int length)
    {
        if (destination == null)
        {
            throw new NullPointerException("destination");
        }

        if (destinationIndex < 0 || length < 0 || destinationIndex > destination.length - length)
        {
            throw new IndexOutOfBoundsException("destinationIndex=" + destinationIndex + ", length=" + length + ", destinationLength=" + destination.length);
        }

        checkRange(sourceIndex, sourceIndex + length);
        UnsafeMemory.copyToHeap(baseAddress() + sourceIndex * Float.BYTES, destination, UnsafeMemory.FLOAT_ARRAY_BASE + (long) destinationIndex * Float.BYTES, (long) length * Float.BYTES);
    }

    private void quicksort(long low, long high)
    {
        while (high - low >= 16)
        {
            long middle = low + ((high - low) >>> 1);
            float pivot = getUnchecked(medianOfThree(low, middle, high));

            long left = low;
            long right = high;

            while (left <= right)
            {
                while (Float.compare(getUnchecked(left), pivot) < 0)
                {
                    left++;
                }

                while (Float.compare(getUnchecked(right), pivot) > 0)
                {
                    right--;
                }

                if (left <= right)
                {
                    swap(left, right);
                    left++;
                    right--;
                }
            }

            if (right - low < high - left)
            {
                quicksort(low, right);
                low = left;
            }
            else
            {
                quicksort(left, high);
                high = right;
            }
        }

        insertionSort(low, high);
    }

    private void insertionSort(long low, long high)
    {
        for (long i = low + 1; i <= high; i++)
        {
            float value = getUnchecked(i);
            long j = i - 1;

            while (j >= low && Float.compare(getUnchecked(j), value) > 0)
            {
                setUnchecked(j + 1, getUnchecked(j));
                j--;
            }

            setUnchecked(j + 1, value);
        }
    }

    private long medianOfThree(long a, long b, long c)
    {
        float first = getUnchecked(a);
        float second = getUnchecked(b);
        float third = getUnchecked(c);

        if (Float.compare(first, second) < 0)
        {
            return Float.compare(third, first) < 0 ? a : (Float.compare(third, second) < 0 ? c : b);
        }

        return Float.compare(third, second) < 0 ? b : (Float.compare(third, first) < 0 ? c : a);
    }

    private void swap(long i, long j)
    {
        float temp = getUnchecked(i);
        setUnchecked(i, getUnchecked(j));
        setUnchecked(j, temp);
    }
}
