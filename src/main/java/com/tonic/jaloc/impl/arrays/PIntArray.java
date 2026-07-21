package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

/**
 * A fixed-length native int array.
 */
public final class PIntArray extends AbstractPrimitiveArray<PIntWriter>
{
    /**
     * Allocates length elements on the system allocator, zeroed.
     *
     * @param length the element count
     * @throws IllegalArgumentException if length is negative
     */
    public PIntArray(long length)
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
    public PIntArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.DWORD, length);
    }

    /**
     * Reads the element at index.
     *
     * @param index the element index
     * @return the element
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException if closed
     */
    public int get(long index)
    {
        return readInt(index);
    }

    /**
     * Writes the element at index.
     *
     * @param index the element index
     * @param value the value to store
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException if closed
     */
    public void set(long index, int value)
    {
        writeInt(index, value);
    }

    /**
     * Reads the element at index with no liveness or bounds check; the caller must have proven both.
     *
     * @param index the element index
     * @return the element
     */
    public int getUnchecked(long index)
    {
        return readIntUnchecked(index);
    }

    /**
     * Writes the element at index with no liveness or bounds check; the caller must have proven both.
     *
     * @param index the element index
     * @param value the value to store
     */
    public void setUnchecked(long index, int value)
    {
        writeIntUnchecked(index, value);
    }

    @Override
    public PIntWriter writer()
    {
        return new PIntWriter(this);
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
        long source = baseAddress() + fromIndex * Integer.BYTES;
        long scratch = UnsafeMemory.allocate(length * Integer.BYTES);

        try
        {
            long[] counts = new long[4 * 256];

            for (long i = 0; i < length; i++)
            {
                int value = UnsafeMemory.getInt(source + i * Integer.BYTES);

                counts[value & 0xFF]++;
                counts[256 + ((value >>> 8) & 0xFF)]++;
                counts[512 + ((value >>> 16) & 0xFF)]++;
                counts[768 + (((value >>> 24) ^ 0x80) & 0xFF)]++;
            }

            long from = source;
            long to = scratch;

            for (int pass = 0; pass < 4; pass++)
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
                boolean signedPass = pass == 3;

                for (long i = 0; i < length; i++)
                {
                    int value = UnsafeMemory.getInt(from + i * Integer.BYTES);
                    int digit = signedPass ? ((value >>> shift) ^ 0x80) & 0xFF : (value >>> shift) & 0xFF;

                    UnsafeMemory.putInt(to + offsets[digit] * Integer.BYTES, value);
                    offsets[digit]++;
                }

                long swap = from;

                from = to;
                to = swap;
            }

            if (from != source)
            {
                UnsafeMemory.copy(from, source, length * Integer.BYTES);
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
    public long binarySearch(int value)
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
    public long binarySearch(long fromIndex, long toIndex, int value)
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
            int midValue = getUnchecked(mid);

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

    private long branchlessSearch(long fromIndex, long toIndex, int value)
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
            int candidate = getUnchecked(base);

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
    public void copyFrom(int[] source, int sourceIndex, long destinationIndex, int length)
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
        UnsafeMemory.copyFromHeap(source, UnsafeMemory.INT_ARRAY_BASE + (long) sourceIndex * Integer.BYTES, baseAddress() + destinationIndex * Integer.BYTES, (long) length * Integer.BYTES);
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
    public void copyTo(long sourceIndex, int[] destination, int destinationIndex, int length)
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
        UnsafeMemory.copyToHeap(baseAddress() + sourceIndex * Integer.BYTES, destination, UnsafeMemory.INT_ARRAY_BASE + (long) destinationIndex * Integer.BYTES, (long) length * Integer.BYTES);
    }

    private void quicksort(long low, long high)
    {
        while (high - low >= 16)
        {
            long middle = low + ((high - low) >>> 1);
            int pivot = getUnchecked(medianOfThree(low, middle, high));

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
            int value = getUnchecked(i);
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
        int first = getUnchecked(a);
        int second = getUnchecked(b);
        int third = getUnchecked(c);

        if (first < second)
        {
            return third < first ? a : (third < second ? c : b);
        }

        return third < second ? b : (third < first ? c : a);
    }

    private void swap(long i, long j)
    {
        int temp = getUnchecked(i);
        setUnchecked(i, getUnchecked(j));
        setUnchecked(j, temp);
    }
}
