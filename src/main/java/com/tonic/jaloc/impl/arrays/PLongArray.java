package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

public final class PLongArray extends AbstractPrimitiveArray<PLongWriter>
{
    public PLongArray(long length)
    {
        super(ElementSize.QWORD, length);
    }

    public PLongArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.QWORD, length);
    }

    public long get(long index)
    {
        return readLong(index);
    }

    public void set(long index, long value)
    {
        writeLong(index, value);
    }

    public long getUnchecked(long index)
    {
        return readLongUnchecked(index);
    }

    public void setUnchecked(long index, long value)
    {
        writeLongUnchecked(index, value);
    }

    @Override
    public PLongWriter writer()
    {
        return new PLongWriter(this);
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

    public long binarySearch(long value)
    {
        return binarySearch(0, length(), value);
    }

    public long binarySearch(long fromIndex, long toIndex, long value)
    {
        checkRange(fromIndex, toIndex);

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
