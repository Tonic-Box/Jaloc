package com.tonic.jaloc.impl.lists;

import com.tonic.jaloc.impl.arrays.PIntArray;
import com.tonic.jaloc.impl.arrays.PIntWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveList;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

/**
 * A growable native int list.
 */
public final class PIntList extends AbstractPrimitiveList<PIntArray, PIntWriter> {
    /**
     * Creates an empty list with zero capacity on the system allocator.
     */
    public PIntList() {
        this(0);
    }

    /**
     * Creates an empty list with the given starting capacity on the system allocator.
     *
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PIntList(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    /**
     * Creates an empty list with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PIntList(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PIntArray(allocator, initialCapacity));
    }

    @Override
    protected PIntArray createArray(NativeAllocator allocator, long capacity) {
        return new PIntArray(allocator, capacity);
    }

    /**
     * Appends value, growing if needed.
     *
     * @param value the value to append
     * @throws IllegalStateException if closed
     */
    public void add(int value) {
        PIntWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
    }

    /**
     * Appends values left to right in one capacity reservation.
     *
     * @param values the values to append
     * @throws NullPointerException if values is null
     * @throws IllegalStateException if closed
     */
    public void addAll(int... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        addAll(values, 0, values.length);
    }

    /**
     * Appends length values from the slice in one bulk copy.
     *
     * @param values the source array
     * @param offset the start within values
     * @param length the element count
     * @throws NullPointerException if values is null
     * @throws IndexOutOfBoundsException if the slice is out of bounds
     * @throws IllegalStateException if closed
     */
    public void addAll(int[] values, int offset, int length) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (offset < 0 || length < 0 || offset > values.length - length) {
            throw new IndexOutOfBoundsException("offset=" + offset + ", length=" + length + ", valuesLength=" + values.length);
        }

        ensureOpen();

        long s = sizeUnchecked();

        ensureCapacity(Math.addExact(s, length));
        elementsUnchecked().copyFrom(values, offset, s, length);
        size(s + length);
    }

    /**
     * Reads the element at index.
     *
     * @param index the element index
     * @return the element
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException if closed
     */
    public int get(long index)
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
    public int set(long index, int value)
    {
        checkElementIndex(index);
        int previous = elementsUnchecked().getUnchecked(index);
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
    public int removeLast()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        long lastIndex = size() - 1;
        int previous = elementsUnchecked().getUnchecked(lastIndex);
        decrementSize();
        return previous;
    }

    /**
     * Sorts the live range ascending.
     *
     * @throws IllegalStateException if closed
     */
    public void sort()
    {
        elements().sort(0, size());
    }

    /**
     * Binary searches the live range; content must be sorted.
     *
     * @param value the value to find
     * @return the matching index, or -(insertionPoint + 1) if absent
     * @throws IllegalStateException if closed
     */
    public long binarySearch(int value)
    {
        return elements().binarySearch(0, size(), value);
    }
}
