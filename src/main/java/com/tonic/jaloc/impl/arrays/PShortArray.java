package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;

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
        quicksort(fromIndex, toIndex - 1);
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
            short midValue = get(mid);

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
            short pivot = get(medianOfThree(low, middle, high));

            long left = low;
            long right = high;

            while (left <= right)
            {
                while (get(left) < pivot)
                {
                    left++;
                }

                while (get(right) > pivot)
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
            short value = get(i);
            long j = i - 1;

            while (j >= low && get(j) > value)
            {
                set(j + 1, get(j));
                j--;
            }

            set(j + 1, value);
        }
    }

    private long medianOfThree(long a, long b, long c)
    {
        short first = get(a);
        short second = get(b);
        short third = get(c);

        if (first < second)
        {
            return third < first ? a : (third < second ? c : b);
        }

        return third < second ? b : (third < first ? c : a);
    }

    private void swap(long i, long j)
    {
        short temp = get(i);
        set(i, get(j));
        set(j, temp);
    }
}
