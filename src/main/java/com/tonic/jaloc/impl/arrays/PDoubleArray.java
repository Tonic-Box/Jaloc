package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;

public final class PDoubleArray extends AbstractPrimitiveArray<PDoubleWriter>
{
    public PDoubleArray(long length)
    {
        super(ElementSize.QWORD, length);
    }

    public PDoubleArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.QWORD, length);
    }

    public double get(long index)
    {
        return readDouble(index);
    }

    public void set(long index, double value)
    {
        writeDouble(index, value);
    }

    public double getUnchecked(long index)
    {
        return readDoubleUnchecked(index);
    }

    public void setUnchecked(long index, double value)
    {
        writeDoubleUnchecked(index, value);
    }

    @Override
    public PDoubleWriter writer()
    {
        return new PDoubleWriter(this);
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

    public long binarySearch(double value)
    {
        return binarySearch(0, length(), value);
    }

    public long binarySearch(long fromIndex, long toIndex, double value)
    {
        checkRange(fromIndex, toIndex);

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
