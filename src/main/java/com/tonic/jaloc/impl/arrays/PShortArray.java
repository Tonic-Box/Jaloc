package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.FileMappedAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

import java.nio.file.Path;

/**
 * A fixed-length native short array.
 */
public final class PShortArray extends AbstractPrimitiveArray<PShortWriter>
{
    /**
     * Allocates length elements on the system allocator, zeroed.
     *
     * @param length the element count
     * @throws IllegalArgumentException if length is negative
     */
    public PShortArray(long length)
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
    public PShortArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.WORD, length);
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
    public static PShortArray create(Path path, long length)
    {
        return new PShortArray(FileMappedAllocator.create(path, ElementSize.WORD.byteSize(MappedArrays.requireLength(length))), length);
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
    public static PShortArray open(Path path)
    {
        FileMappedAllocator allocator = FileMappedAllocator.open(path);
        return new PShortArray(allocator, MappedArrays.elementCount(allocator.fileBytes(), ElementSize.WORD.getSize()));
    }

    /**
     * Reads the element at index.
     *
     * @param index the element index
     * @return the element
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException if closed
     */
    public short get(long index)
    {
        return readShort(index);
    }

    /**
     * Writes the element at index.
     *
     * @param index the element index
     * @param value the value to store
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException if closed
     */
    public void set(long index, short value)
    {
        writeShort(index, value);
    }

    /**
     * Reads the element at index with no liveness or bounds check; the caller must have proven both.
     *
     * @param index the element index
     * @return the element
     */
    public short getUnchecked(long index)
    {
        return readShortUnchecked(index);
    }

    /**
     * Writes the element at index with no liveness or bounds check; the caller must have proven both.
     *
     * @param index the element index
     * @param value the value to store
     */
    public void setUnchecked(long index, short value)
    {
        writeShortUnchecked(index, value);
    }

    @Override
    public PShortWriter writer()
    {
        return new PShortWriter(this);
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
        long base = baseAddress() + fromIndex * Short.BYTES;
        long length = toIndex - fromIndex;
        long[] counts = new long[65536];

        for (long i = 0; i < length; i++)
        {
            counts[UnsafeMemory.getShort(base + i * Short.BYTES) + 32768]++;
        }

        long position = base;

        for (int bucket = 0; bucket < 65536; bucket++)
        {
            long run = counts[bucket];
            short value = (short) (bucket - 32768);

            for (long k = 0; k < run; k++)
            {
                UnsafeMemory.putShort(position, value);
                position += Short.BYTES;
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
    public long binarySearch(short value)
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
    public long binarySearch(long fromIndex, long toIndex, short value)
    {
        checkRange(fromIndex, toIndex);

        if (toIndex - fromIndex <= 1048576)
        {
            return branchlessSearch(fromIndex, toIndex, value);
        }

        long low = fromIndex;
        long high = toIndex - 1;

        while (low <= high)
        {
            long mid = low + ((high - low) >>> 1);
            short midValue = getUnchecked(mid);

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

    private long branchlessSearch(long fromIndex, long toIndex, short value)
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
            short candidate = getUnchecked(base);

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
    public void copyFrom(short[] source, int sourceIndex, long destinationIndex, int length)
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
        UnsafeMemory.copyFromHeap(source, UnsafeMemory.SHORT_ARRAY_BASE + (long) sourceIndex * Short.BYTES, baseAddress() + destinationIndex * Short.BYTES, (long) length * Short.BYTES);
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
    public void copyTo(long sourceIndex, short[] destination, int destinationIndex, int length)
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
        UnsafeMemory.copyToHeap(baseAddress() + sourceIndex * Short.BYTES, destination, UnsafeMemory.SHORT_ARRAY_BASE + (long) destinationIndex * Short.BYTES, (long) length * Short.BYTES);
    }

    private void quicksort(long low, long high)
    {
        while (high - low >= 16)
        {
            long middle = low + ((high - low) >>> 1);
            short pivot = getUnchecked(medianOfThree(low, middle, high));

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
            short value = getUnchecked(i);
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
        short first = getUnchecked(a);
        short second = getUnchecked(b);
        short third = getUnchecked(c);

        if (first < second)
        {
            return third < first ? a : (third < second ? c : b);
        }

        return third < second ? b : (third < first ? c : a);
    }

    private void swap(long i, long j)
    {
        short temp = getUnchecked(i);
        setUnchecked(i, getUnchecked(j));
        setUnchecked(j, temp);
    }
}
