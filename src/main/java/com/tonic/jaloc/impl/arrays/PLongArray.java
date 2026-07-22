package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

/**
 * A fixed-length native long array.
 */
public final class PLongArray extends AbstractPrimitiveArray<PLongWriter>
{
    /**
     * Allocates length elements on the system allocator, zeroed.
     *
     * @param length the element count
     * @throws IllegalArgumentException if length is negative
     */
    public PLongArray(long length)
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
    public PLongArray(NativeAllocator allocator, long length)
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
    public long get(long index)
    {
        return readLong(index);
    }

    /**
     * Writes the element at index.
     *
     * @param index the element index
     * @param value the value to store
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException if closed
     */
    public void set(long index, long value)
    {
        writeLong(index, value);
    }

    /**
     * Reads the element at index with no liveness or bounds check; the caller must have proven both.
     *
     * @param index the element index
     * @return the element
     */
    public long getUnchecked(long index)
    {
        return readLongUnchecked(index);
    }

    /**
     * Writes the element at index with no liveness or bounds check; the caller must have proven both.
     *
     * @param index the element index
     * @param value the value to store
     */
    public void setUnchecked(long index, long value)
    {
        writeLongUnchecked(index, value);
    }

    @Override
    public PLongWriter writer()
    {
        return new PLongWriter(this);
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

        radixSort(fromIndex, toIndex);
    }

    private void radixSort(long fromIndex, long toIndex)
    {
        long length = toIndex - fromIndex;
        long source = baseAddress() + fromIndex * Long.BYTES;
        long scratch = UnsafeMemory.allocate(length * Long.BYTES);

        try
        {
            long[] counts = new long[8 * 256];

            for (long i = 0; i < length; i++)
            {
                long value = UnsafeMemory.getLong(source + i * Long.BYTES);

                for (int pass = 0; pass < 7; pass++)
                {
                    counts[(pass << 8) + (int) ((value >>> (pass << 3)) & 0xFF)]++;
                }

                counts[(7 << 8) + (int) (((value >>> 56) ^ 0x80) & 0xFF)]++;
            }

            long from = source;
            long to = scratch;

            for (int pass = 0; pass < 8; pass++)
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
                boolean signedPass = pass == 7;

                for (long i = 0; i < length; i++)
                {
                    long value = UnsafeMemory.getLong(from + i * Long.BYTES);
                    int digit = signedPass ? (int) (((value >>> shift) ^ 0x80) & 0xFF) : (int) ((value >>> shift) & 0xFF);

                    UnsafeMemory.putLong(to + offsets[digit] * Long.BYTES, value);
                    offsets[digit]++;
                }

                long swap = from;

                from = to;
                to = swap;
            }

            if (from != source)
            {
                UnsafeMemory.copy(from, source, length * Long.BYTES);
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
    public long binarySearch(long value)
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
    public long binarySearch(long fromIndex, long toIndex, long value)
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
            long midValue = getUnchecked(mid);

            if (midValue < value)
            {
                low = mid + 1;
            }
            else if (midValue > value)
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

    private long branchlessSearch(long fromIndex, long toIndex, long value)
    {
        long base = fromIndex;
        long n = toIndex - fromIndex;

        while (n > 1)
        {
            long half = n >>> 1;

            base = getUnchecked(base + half - 1) < value ? base + half : base;
            n -= half;
        }

        if (n == 1)
        {
            long candidate = getUnchecked(base);

            if (candidate == value)
            {
                return base;
            }

            if (candidate < value)
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
    public void copyFrom(long[] source, int sourceIndex, long destinationIndex, int length)
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
        UnsafeMemory.copyFromHeap(source, UnsafeMemory.LONG_ARRAY_BASE + (long) sourceIndex * Long.BYTES, baseAddress() + destinationIndex * Long.BYTES, (long) length * Long.BYTES);
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
    public void copyTo(long sourceIndex, long[] destination, int destinationIndex, int length)
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
        UnsafeMemory.copyToHeap(baseAddress() + sourceIndex * Long.BYTES, destination, UnsafeMemory.LONG_ARRAY_BASE + (long) destinationIndex * Long.BYTES, (long) length * Long.BYTES);
    }

    private void quicksort(long low, long high)
    {
        while (high - low >= 16)
        {
            long middle = low + ((high - low) >>> 1);
            long pivot = getUnchecked(medianOfThree(low, middle, high));

            long left = low;
            long right = high;

            while (left <= right)
            {
                while (getUnchecked(left) < pivot)
                {
                    left++;
                }

                while (getUnchecked(right) > pivot)
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
            long value = getUnchecked(i);
            long j = i - 1;

            while (j >= low && getUnchecked(j) > value)
            {
                setUnchecked(j + 1, getUnchecked(j));
                j--;
            }

            setUnchecked(j + 1, value);
        }
    }

    private long medianOfThree(long a, long b, long c)
    {
        long first = getUnchecked(a);
        long second = getUnchecked(b);
        long third = getUnchecked(c);

        if (first < second)
        {
            return third < first ? a : (third < second ? c : b);
        }

        return third < second ? b : (third < first ? c : a);
    }

    private void swap(long i, long j)
    {
        long temp = getUnchecked(i);
        setUnchecked(i, getUnchecked(j));
        setUnchecked(j, temp);
    }
}
