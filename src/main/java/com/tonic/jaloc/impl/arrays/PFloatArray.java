package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;

public final class PFloatArray extends AbstractPrimitiveArray<PFloatWriter>
{
    public PFloatArray(long length)
    {
        super(ElementSize.DWORD, length);
    }

    public PFloatArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.DWORD, length);
    }

    public float get(long index)
    {
        return readFloat(index);
    }

    public void set(long index, float value)
    {
        writeFloat(index, value);
    }

    public float getUnchecked(long index)
    {
        return readFloatUnchecked(index);
    }

    public void setUnchecked(long index, float value)
    {
        writeFloatUnchecked(index, value);
    }

    @Override
    public PFloatWriter writer()
    {
        return new PFloatWriter(this);
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

    public long binarySearch(float value)
    {
        return binarySearch(0, length(), value);
    }

    public long binarySearch(long fromIndex, long toIndex, float value)
    {
        checkRange(fromIndex, toIndex);

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
