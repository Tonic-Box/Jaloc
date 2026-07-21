package com.tonic.jaloc.impl.lists;

import com.tonic.jaloc.impl.arrays.PFloatArray;
import com.tonic.jaloc.impl.arrays.PFloatWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveList;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

import java.util.NoSuchElementException;

/**
 * A growable native float list.
 */
public final class PFloatList extends AbstractPrimitiveList<PFloatArray, PFloatWriter>
{
    /**
     * Creates an empty list with zero capacity on the system allocator.
     */
    public PFloatList() {
        this(0);
    }

    /**
     * Creates an empty list with the given starting capacity on the system allocator.
     *
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PFloatList(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    /**
     * Creates an empty list with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PFloatList(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PFloatArray(allocator, initialCapacity));
    }

    @Override
    protected PFloatArray createArray(NativeAllocator allocator, long capacity) {
        return new PFloatArray(allocator, capacity);
    }

    /**
     * Appends value, growing if needed.
     *
     * @param value the value to append
     * @throws IllegalStateException if closed
     */
    public void add(float value) {
        long s = appendIndex();
        UnsafeMemory.putFloat(elementsBase() + (s << 2), value);
        size(s + 1);
    }

    /**
     * Appends values left to right in one capacity reservation.
     *
     * @param values the values to append
     * @throws NullPointerException if values is null
     * @throws IllegalStateException if closed
     */
    public void addAll(float... values) {
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
    public void addAll(float[] values, int offset, int length) {
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
    public float get(long index)
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
    public float set(long index, float value)
    {
        checkElementIndex(index);
        float previous = elementsUnchecked().getUnchecked(index);
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
    public float removeLast()
    {
        ensureOpen();
        long s = sizeUnchecked();
        if (s == 0) {
            throw new NoSuchElementException("List is empty");
        }
        float previous = UnsafeMemory.getFloat(elementsBase() + ((s - 1) << 2));
        size(s - 1);
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
    public long binarySearch(float value)
    {
        return elements().binarySearch(0, size(), value);
    }
}
