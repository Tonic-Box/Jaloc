package com.tonic.jaloc.impl.lists;

import com.tonic.jaloc.impl.arrays.PShortArray;
import com.tonic.jaloc.impl.arrays.PShortWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveList;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.IntConsumer;

/**
 * A growable native short list.
 */
public final class PShortList extends AbstractPrimitiveList<PShortArray, PShortWriter>
{
    /**
     * Creates an empty list with zero capacity on the system allocator.
     */
    public PShortList() {
        this(0);
    }

    /**
     * Creates an empty list with the given starting capacity on the system allocator.
     *
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PShortList(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    /**
     * Creates an empty list with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PShortList(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PShortArray(allocator, initialCapacity));
    }

    @Override
    protected PShortArray createArray(NativeAllocator allocator, long capacity) {
        return new PShortArray(allocator, capacity);
    }

    /**
     * Appends value, growing if needed.
     *
     * @param value the value to append
     * @throws IllegalStateException if closed
     */
    public void add(short value) {
        long s = appendIndex();
        UnsafeMemory.putShort(elementsBase() + (s << 1), value);
        size(s + 1);
    }

    /**
     * Appends values left to right in one capacity reservation.
     *
     * @param values the values to append
     * @throws NullPointerException if values is null
     * @throws IllegalStateException if closed
     */
    public void addAll(short... values) {
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
    public void addAll(short[] values, int offset, int length) {
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
    public short get(long index)
    {
        checkElementIndex(index);
        return UnsafeMemory.getShort(elementsBase() + (index << 1));
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
    public short set(long index, short value)
    {
        checkElementIndex(index);

        long address = elementsBase() + (index << 1);
        short previous = UnsafeMemory.getShort(address);

        UnsafeMemory.putShort(address, value);
        return previous;
    }

    /**
     * Removes and returns the last element.
     *
     * @return the removed element
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public short removeLast()
    {
        ensureOpen();
        long s = sizeUnchecked();
        if (s == 0) {
            throw new NoSuchElementException("List is empty");
        }
        short previous = UnsafeMemory.getShort(elementsBase() + ((s - 1) << 1));
        size(s - 1);
        return previous;
    }

    /**
     * Emits each element first to last.
     *
     * @param consumer the receiver
     * @throws NullPointerException if consumer is null
     * @throws IllegalStateException if closed
     */
    public void forEach(IntConsumer consumer)
    {
        Objects.requireNonNull(consumer, "consumer");
        ensureOpen();

        long base = elementsBase();
        long s = sizeUnchecked();
        short[] buffer = new short[(int) Math.min(1024, s)];
        long index = 0;

        while (index < s)
        {
            int chunk = (int) Math.min(buffer.length, s - index);

            UnsafeMemory.copyToHeap(base + (index << 1), buffer, UnsafeMemory.SHORT_ARRAY_BASE, (long) chunk << 1);

            for (int i = 0; i < chunk; i++)
            {
                consumer.accept(buffer[i]);
            }

            index += chunk;
        }
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
    public long binarySearch(short value)
    {
        return elements().binarySearch(0, size(), value);
    }
}
