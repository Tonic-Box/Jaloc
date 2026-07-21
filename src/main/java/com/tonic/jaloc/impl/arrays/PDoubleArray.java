package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

/**
 * A fixed-length native double array.
 */
public final class PDoubleArray extends AbstractPrimitiveArray<PDoubleWriter>
{
    /**
     * Allocates length elements on the system allocator, zeroed.
     *
     * @param length the element count
     * @throws IllegalArgumentException if length is negative
     */
    public PDoubleArray(long length)
    {
        super(ElementSize.QWORD, length);
    }

    /**
     * Allocates length elements on the given allocator, zeroed.
     *
     * @param allocator the allocator to source memory from
     * @param length the element count
     * @throws IllegalArgumentException if length is negative
     */
    public PDoubleArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.QWORD, length);
    }

    /**
     * Reads the element at index.
     *
     * @param index the element index
     * @return the element
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException if closed
     */
    public double get(long index)
    {
        return readDouble(index);
    }

    /**
     * Writes the element at index.
     *
     * @param index the element index
     * @param value the value to store
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException if closed
     */
    public void set(long index, double value)
    {
        writeDouble(index, value);
    }

    /**
     * Reads the element at index with no liveness or bounds check; the caller must have proven both.
     *
     * @param index the element index
     * @return the element
     */
    public double getUnchecked(long index)
    {
        return readDoubleUnchecked(index);
    }

    /**
     * Writes the element at index with no liveness or bounds check; the caller must have proven both.
     *
     * @param index the element index
     * @param value the value to store
     */
    public void setUnchecked(long index, double value)
    {
        writeDoubleUnchecked(index, value);
    }

    @Override
    public PDoubleWriter writer()
    {
        return new PDoubleWriter(this);
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
            double value = getUnchecked(i);

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
        long source = baseAddress() + fromIndex * Double.BYTES;
        long scratch = UnsafeMemory.allocate(length * Double.BYTES);

        try
        {
            long[] counts = new long[8 * 256];

            for (long i = 0; i < length; i++)
            {
                long bits = UnsafeMemory.getLong(source + i * Double.BYTES);
                long value = bits ^ ((bits >> 63) | 0x8000000000000000L);

                UnsafeMemory.putLong(source + i * Double.BYTES, value);

                for (int pass = 0; pass < 8; pass++)
                {
                    counts[(pass << 8) + (int) ((value >>> (pass << 3)) & 0xFF)]++;
                }
            }

            long from = source;
            long to = scratch;

            for (int pass = 0; pass < 8; pass++)
            {
                int bucketBase = pass << 8;

                if (trivialPass(counts, bucketBase, length))
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
                    long value = UnsafeMemory.getLong(from + i * Double.BYTES);
                    int digit = (int) ((value >>> shift) & 0xFF);

                    UnsafeMemory.putLong(to + offsets[digit] * Double.BYTES, value);
                    offsets[digit]++;
                }

                long swap = from;

                from = to;
                to = swap;
            }

            for (long i = 0; i < length; i++)
            {
                long value = UnsafeMemory.getLong(from + i * Double.BYTES);

                UnsafeMemory.putLong(source + i * Double.BYTES, value ^ ((~value >> 63) | 0x8000000000000000L));
            }
        }
        finally
        {
            UnsafeMemory.free(scratch);
        }
    }

    private static boolean trivialPass(long[] counts, int bucketBase, long length)
    {
        for (int bucket = 0; bucket < 256; bucket++)
        {
            if (counts[bucketBase + bucket] == length)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Binary searches the whole array; content must be sorted.
     *
     * @param value the value to find
     * @return the matching index, or -(insertionPoint + 1) if absent
     * @throws IllegalStateException if closed
     */
    public long binarySearch(double value)
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
    public long binarySearch(long fromIndex, long toIndex, double value)
    {
        checkRange(fromIndex, toIndex);

        if (toIndex - fromIndex <= 262144)
        {
            return branchlessSearch(fromIndex, toIndex, value);
        }

        long low = fromIndex;
        long high = toIndex - 1;

        while (low <= high)
        {
            long mid = low + ((high - low) >>> 1);
            int comparison = Double.compare(getUnchecked(mid), value);

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

    private long branchlessSearch(long fromIndex, long toIndex, double value)
    {
        long base = fromIndex;
        long n = toIndex - fromIndex;

        while (n > 1)
        {
            long half = n >>> 1;

            base = Double.compare(getUnchecked(base + half - 1), value) < 0 ? base + half : base;
            n -= half;
        }

        if (n == 1)
        {
            int comparison = Double.compare(getUnchecked(base), value);

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
    public void copyFrom(double[] source, int sourceIndex, long destinationIndex, int length)
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
        UnsafeMemory.copyFromHeap(source, UnsafeMemory.DOUBLE_ARRAY_BASE + (long) sourceIndex * Double.BYTES, baseAddress() + destinationIndex * Double.BYTES, (long) length * Double.BYTES);
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
    public void copyTo(long sourceIndex, double[] destination, int destinationIndex, int length)
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
        UnsafeMemory.copyToHeap(baseAddress() + sourceIndex * Double.BYTES, destination, UnsafeMemory.DOUBLE_ARRAY_BASE + (long) destinationIndex * Double.BYTES, (long) length * Double.BYTES);
    }

    private void quicksort(long low, long high)
    {
        while (high - low >= 16)
        {
            long middle = low + ((high - low) >>> 1);
            double pivot = getUnchecked(medianOfThree(low, middle, high));

            long left = low;
            long right = high;

            while (left <= right)
            {
                while (Double.compare(getUnchecked(left), pivot) < 0)
                {
                    left++;
                }

                while (Double.compare(getUnchecked(right), pivot) > 0)
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
            double value = getUnchecked(i);
            long j = i - 1;

            while (j >= low && Double.compare(getUnchecked(j), value) > 0)
            {
                setUnchecked(j + 1, getUnchecked(j));
                j--;
            }

            setUnchecked(j + 1, value);
        }
    }

    private long medianOfThree(long a, long b, long c)
    {
        double first = getUnchecked(a);
        double second = getUnchecked(b);
        double third = getUnchecked(c);

        if (Double.compare(first, second) < 0)
        {
            return Double.compare(third, first) < 0 ? a : (Double.compare(third, second) < 0 ? c : b);
        }

        return Double.compare(third, second) < 0 ? b : (Double.compare(third, first) < 0 ? c : a);
    }

    private void swap(long i, long j)
    {
        double temp = getUnchecked(i);
        setUnchecked(i, getUnchecked(j));
        setUnchecked(j, temp);
    }
}
