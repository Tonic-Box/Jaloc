package com.tonic.jaloc.impl.dequeues;

import com.tonic.jaloc.impl.arrays.PBoolArray;
import com.tonic.jaloc.impl.arrays.PBoolWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveDeque;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

/**
 * A growable native bool deque over a bit-packed ring.
 */
public final class PBoolDeque extends AbstractPrimitiveDeque<PBoolArray, PBoolWriter>
{
    /**
     * Creates an empty deque with zero capacity on the system allocator.
     */
    public PBoolDeque()
    {
        this(0);
    }

    /**
     * Creates an empty deque with the given starting capacity on the system allocator.
     *
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PBoolDeque(long initialCapacity)
    {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    /**
     * Creates an empty deque with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PBoolDeque(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PBoolArray(allocator, initialCapacity));
    }

    @Override
    protected PBoolArray createArray(NativeAllocator allocator, long capacity) {
        return new PBoolArray(allocator, capacity);
    }

    @Override
    protected void migrateElements(PBoolArray source, PBoolArray destination)
    {
        long size = size();

        for (long i = 0; i < size; i++)
        {
            destination.set(i, source.get(physicalIndex(i)));
        }
    }

    /**
     * Grows capacity to at least requiredCapacity.
     *
     * @param requiredCapacity the minimum capacity
     * @throws IllegalArgumentException if requiredCapacity is negative
     * @throws IllegalStateException if closed
     */
    public void ensureCapacity(long requiredCapacity)
    {
        ensureRingCapacity(requiredCapacity);
    }

    /**
     * Shrinks capacity to the current size.
     *
     * @throws IllegalStateException if closed
     */
    public void trimToSize()
    {
        trimRing();
    }

    /**
     * Adds value at the head, growing if needed.
     *
     * @param value the value to add
     * @throws IllegalStateException if closed
     */
    public void addFirst(boolean value)
    {
        long index = reserveHead();
        elementsUnchecked().setUnchecked(index, value);
        commitHead();
    }

    /**
     * Adds value at the tail, growing if needed.
     *
     * @param value the value to add
     * @throws IllegalStateException if closed
     */
    public void addLast(boolean value)
    {
        long index = reserveTail();
        elementsUnchecked().setUnchecked(index, value);
        commitTail();
    }

    /**
     * Removes and returns the head element.
     *
     * @return the removed element
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public boolean removeFirst()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = headIndex();
        boolean value = elementsUnchecked().getUnchecked(index);
        advanceHead();
        return value;
    }

    /**
     * Removes and returns the tail element.
     *
     * @return the removed element
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public boolean removeLast()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = tailIndex();
        boolean value = elementsUnchecked().getUnchecked(index);
        shrinkTail();
        return value;
    }

    /**
     * Reads the head element without removing it.
     *
     * @return the head element
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public boolean peekFirst()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elementsUnchecked().getUnchecked(headIndex());
    }

    /**
     * Reads the tail element without removing it.
     *
     * @return the tail element
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public boolean peekLast()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elementsUnchecked().getUnchecked(tailIndex());
    }
}
