package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

public final class PShortArray extends AbstractPrimitiveArray<PShortWriter>
{
    public PShortArray(long length)
    {
        super(ElementSize.WORD, length);
    }

    public PShortArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.WORD, length);
    }

    public short get(long index)
    {
        return readShort(index);
    }

    public void set(long index, short value)
    {
        writeShort(index, value);
    }

    public short getUnchecked(long index)
    {
        return readShortUnchecked(index);
    }

    public void setUnchecked(long index, short value)
    {
        writeShortUnchecked(index, value);
    }

    @Override
    public PShortWriter writer()
    {
        return new PShortWriter(this);
    }

    public void sort()
    {
        sort(0, length());
    }

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

    public long binarySearch(short value)
    {
        return binarySearch(0, length(), value);
    }

    public long binarySearch(long fromIndex, long toIndex, short value)
    {
        checkRange(fromIndex, toIndex);

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
