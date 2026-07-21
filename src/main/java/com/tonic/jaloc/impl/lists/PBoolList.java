package com.tonic.jaloc.impl.lists;

import com.tonic.jaloc.impl.arrays.PBoolArray;
import com.tonic.jaloc.impl.arrays.PBoolWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveList;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A growable native bool list over a bit-packed array.
 */
public final class PBoolList extends AbstractPrimitiveList<PBoolArray, PBoolWriter>
{
    /**
     * Creates an empty list with zero capacity on the system allocator.
     */
    public PBoolList()
    {
        this(0);
    }

    /**
     * Creates an empty list with the given starting capacity on the system allocator.
     *
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PBoolList(long initialCapacity)
    {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    /**
     * Creates an empty list with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PBoolList(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PBoolArray(allocator, initialCapacity));
    }

    @Override
    protected PBoolArray createArray(NativeAllocator allocator, long capacity) {
        return new PBoolArray(allocator, capacity);
    }

    /**
     * Appends value, growing if needed.
     *
     * @param value the value to append
     * @throws IllegalStateException if closed
     */
    public void add(boolean value)
    {
        long s = appendIndex();
        elementsUnchecked().setUnchecked(s, value);
        size(s + 1);
    }

    /**
     * Appends values left to right in one capacity reservation.
     *
     * @param values the values to append
     * @throws NullPointerException if values is null
     * @throws IllegalStateException if closed
     */
    public void addAll(boolean... values)
    {
        Objects.requireNonNull(values, "values");
        if (values.length == 0) {
            return;
        }
        PBoolWriter writer = appendWriter(values.length);
        for (boolean value : values) {
            writer.put(value);
        }
        commitWriter();
    }

    /**
     * Reads the element at index.
     *
     * @param index the element index
     * @return the element
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException if closed
     */
    public boolean get(long index)
    {
        checkElementIndex(index);
        return elementsUnchecked().getUnchecked(index);
    }

    /**
     * Replaces the element at index.
     *
     * @param index the element index
     * @param value the new value
     * @return the previous value
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException if closed
     */
    public boolean set(long index, boolean value)
    {
        checkElementIndex(index);
        boolean previous = elementsUnchecked().getUnchecked(index);
        elementsUnchecked().setUnchecked(index, value);
        return previous;
    }

    /**
     * Removes and returns the last element.
     *
     * @return the removed element
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public boolean removeLast()
    {
        ensureOpen();
        long s = sizeUnchecked();
        if (s == 0) {
            throw new NoSuchElementException("List is empty");
        }
        boolean previous = elementsUnchecked().getUnchecked(s - 1);
        elementsUnchecked().setUnchecked(s - 1, false);
        size(s - 1);
        return previous;
    }

    /**
     * Counts true elements.
     *
     * @return the number of true elements
     * @throws IllegalStateException if closed
     */
    public long popCount()
    {
        ensureOpen();

        long base = elementsBase();
        long bytes = (sizeUnchecked() + 7) >>> 3;
        long count = 0;
        long index = 0;

        while (index + 8 <= bytes)
        {
            count += Long.bitCount(UnsafeMemory.getLong(base + index));
            index += 8;
        }

        while (index < bytes)
        {
            count += Integer.bitCount(UnsafeMemory.getByte(base + index) & 0xFF);
            index++;
        }

        return count;
    }

    /**
     * Counts true elements from fromIndex inclusive to toIndex exclusive.
     *
     * @param fromIndex the range start, inclusive
     * @param toIndex the range end, exclusive
     * @return the number of true elements in the range
     * @throws IndexOutOfBoundsException if the range is out of bounds
     * @throws IllegalStateException if closed
     */
    public long popCount(long fromIndex, long toIndex)
    {
        ensureOpen();

        long s = sizeUnchecked();

        if (fromIndex < 0 || toIndex < fromIndex || toIndex > s)
        {
            throw new IndexOutOfBoundsException("fromIndex=" + fromIndex + ", toIndex=" + toIndex + ", size=" + s);
        }

        if (fromIndex == toIndex)
        {
            return 0;
        }

        long base = elementsBase();
        long firstByte = fromIndex >>> 3;
        long lastByte = (toIndex - 1) >>> 3;
        int firstMask = (0xFF << (fromIndex & 7)) & 0xFF;
        int lastMask = (toIndex & 7) == 0 ? 0xFF : (1 << (toIndex & 7)) - 1;

        if (firstByte == lastByte)
        {
            return Integer.bitCount(UnsafeMemory.getByte(base + firstByte) & firstMask & lastMask);
        }

        long count = Integer.bitCount(UnsafeMemory.getByte(base + firstByte) & firstMask);
        long index = firstByte + 1;

        while (index + 8 <= lastByte)
        {
            count += Long.bitCount(UnsafeMemory.getLong(base + index));
            index += 8;
        }

        while (index < lastByte)
        {
            count += Integer.bitCount(UnsafeMemory.getByte(base + index) & 0xFF);
            index++;
        }

        return count + Integer.bitCount(UnsafeMemory.getByte(base + lastByte) & lastMask);
    }
}
