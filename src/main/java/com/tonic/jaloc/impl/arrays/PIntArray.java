package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

public final class PIntArray extends AbstractPrimitiveArray<PIntWriter>
{
    public PIntArray(long length)
    {
        super(ElementSize.DWORD, length);
    }

    public PIntArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.DWORD, length);
    }

    public int get(long index)
    {
        return readInt(index);
    }

    public void set(long index, int value)
    {
        writeInt(index, value);
    }

    public int getUnchecked(long index)
    {
        return readIntUnchecked(index);
    }

    public void setUnchecked(long index, int value)
    {
        writeIntUnchecked(index, value);
    }

    @Override
    public PIntWriter writer()
    {
        return new PIntWriter(this);
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

    public long binarySearch(int value)
    {
        return binarySearch(0, length(), value);
    }

    public long binarySearch(long fromIndex, long toIndex, int value)
    {
        checkRange(fromIndex, toIndex);

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