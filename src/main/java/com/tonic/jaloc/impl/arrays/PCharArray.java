package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

/**
 * A fixed-length native char array.
 */
public final class PCharArray extends AbstractPrimitiveArray<PCharWriter>
{
    /**
     * Allocates length elements on the system allocator, zeroed.
     *
     * @param length the element count
     * @throws IllegalArgumentException if length is negative
     */
    public PCharArray(long length)
    {
        super(ElementSize.WORD, length);
    }

    /**
     * Allocates length elements on the given allocator, zeroed.
     *
     * @param allocator the allocator to source memory from
     * @param length the element count
     * @throws IllegalArgumentException if length is negative
     */
    public PCharArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.WORD, length);
    }

    /**
     * Reads the element at index.
     *
     * @param index the element index
     * @return the element
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException if closed
     */
    public char get(long index)
    {
        return readChar(index);
    }

    /**
     * Writes the element at index.
     *
     * @param index the element index
     * @param value the value to store
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException if closed
     */
    public void set(long index, char value)
    {
        writeChar(index, value);
    }

    /**
     * Reads the element at index with no liveness or bounds check; the caller must have proven both.
     *
     * @param index the element index
     * @return the element
     */
    public char getUnchecked(long index)
    {
        return readCharUnchecked(index);
    }

    /**
     * Writes the element at index with no liveness or bounds check; the caller must have proven both.
     *
     * @param index the element index
     * @param value the value to store
     */
    public void setUnchecked(long index, char value)
    {
        writeCharUnchecked(index, value);
    }

    @Override
    public PCharWriter writer()
    {
        return new PCharWriter(this);
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

        countingSort(fromIndex, toIndex);
    }

    private void countingSort(long fromIndex, long toIndex)
    {
        long base = baseAddress() + fromIndex * Character.BYTES;
        long length = toIndex - fromIndex;
        long[] counts = new long[65536];

        for (long i = 0; i < length; i++)
        {
            counts[UnsafeMemory.getChar(base + i * Character.BYTES)]++;
        }

        long position = base;

        for (int bucket = 0; bucket < 65536; bucket++)
        {
            long run = counts[bucket];
            char value = (char) bucket;

            for (long k = 0; k < run; k++)
            {
                UnsafeMemory.putChar(position, value);
                position += Character.BYTES;
            }
        }
    }

    /**
     * Binary searches the whole array; content must be sorted.
     *
     * @param value the value to find
     * @return the matching index, or -(insertionPoint + 1) if absent
     * @throws IllegalStateException if closed
     */
    public long binarySearch(char value)
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
    public long binarySearch(long fromIndex, long toIndex, char value)
    {
        checkRange(fromIndex, toIndex);

        long low = fromIndex;
        long high = toIndex - 1;

        while (low <= high)
        {
            long mid = low + ((high - low) >>> 1);
            char midValue = getUnchecked(mid);

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
    public void copyFrom(char[] source, int sourceIndex, long destinationIndex, int length)
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
        UnsafeMemory.copyFromHeap(source, UnsafeMemory.CHAR_ARRAY_BASE + (long) sourceIndex * Character.BYTES, baseAddress() + destinationIndex * Character.BYTES, (long) length * Character.BYTES);
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
    public void copyTo(long sourceIndex, char[] destination, int destinationIndex, int length)
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
        UnsafeMemory.copyToHeap(baseAddress() + sourceIndex * Character.BYTES, destination, UnsafeMemory.CHAR_ARRAY_BASE + (long) destinationIndex * Character.BYTES, (long) length * Character.BYTES);
    }

    private void quicksort(long low, long high)
    {
        while (high - low >= 16)
        {
            long middle = low + ((high - low) >>> 1);
            char pivot = getUnchecked(medianOfThree(low, middle, high));

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
            char value = getUnchecked(i);
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
        char first = getUnchecked(a);
        char second = getUnchecked(b);
        char third = getUnchecked(c);

        if (first < second)
        {
            return third < first ? a : (third < second ? c : b);
        }

        return third < second ? b : (third < first ? c : a);
    }

    private void swap(long i, long j)
    {
        char temp = getUnchecked(i);
        setUnchecked(i, getUnchecked(j));
        setUnchecked(j, temp);
    }
}
