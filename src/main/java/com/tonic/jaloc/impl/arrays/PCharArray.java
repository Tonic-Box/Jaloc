package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;

public final class PCharArray extends AbstractPrimitiveArray<PCharWriter>
{
    public PCharArray(long length)
    {
        super(ElementSize.WORD, length);
    }

    public PCharArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.WORD, length);
    }

    public char get(long index)
    {
        return readChar(index);
    }

    public void set(long index, char value)
    {
        writeChar(index, value);
    }

    public char getUnchecked(long index)
    {
        return readCharUnchecked(index);
    }

    public void setUnchecked(long index, char value)
    {
        writeCharUnchecked(index, value);
    }

    @Override
    public PCharWriter writer()
    {
        return new PCharWriter(this);
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

    public long binarySearch(char value)
    {
        return binarySearch(0, length(), value);
    }

    public long binarySearch(long fromIndex, long toIndex, char value)
    {
        checkRange(fromIndex, toIndex);

        long low = fromIndex;
        long high = toIndex - 1;

        while (low <= high)
        {
            long mid = low + ((high - low) >>> 1);
            char midValue = get(mid);

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
            char pivot = get(medianOfThree(low, middle, high));

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
            char value = get(i);
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
        char first = get(a);
        char second = get(b);
        char third = get(c);

        if (first < second)
        {
            return third < first ? a : (third < second ? c : b);
        }

        return third < second ? b : (third < first ? c : a);
    }

    private void swap(long i, long j)
    {
        char temp = get(i);
        set(i, get(j));
        set(j, temp);
    }
}
