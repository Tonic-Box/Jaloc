package com.tonic.jaloc.impl.queues;

import com.tonic.jaloc.impl.arrays.PBoolArray;
import com.tonic.jaloc.impl.arrays.PBoolWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveQueue;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A growable native bool FIFO queue over a bit-packed ring.
 */
public final class PBoolQueue extends AbstractPrimitiveQueue<PBoolArray, PBoolWriter>
{
    /**
     * Creates an empty queue with zero capacity on the system allocator.
     */
    public PBoolQueue()
    {
        this(0);
    }

    /**
     * Creates an empty queue with the given starting capacity on the system allocator.
     *
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PBoolQueue(long initialCapacity)
    {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    /**
     * Creates an empty queue with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PBoolQueue(NativeAllocator allocator, long initialCapacity) {
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
     * Enqueues value at the tail, growing if needed.
     *
     * @param value the value to enqueue
     * @throws IllegalStateException if closed
     */
    public void enqueue(boolean value)
    {
        long index = reserveTail();
        elementsUnchecked().setUnchecked(index, value);
        commitTail();
    }

    /**
     * Enqueues values left to right in one capacity reservation.
     *
     * @param values the values to enqueue
     * @throws NullPointerException if values is null
     * @throws IllegalStateException if closed
     */
    public void enqueueAll(boolean... values)
    {
        Objects.requireNonNull(values, "values");
        if (values.length == 0) {
            return;
        }
        ensureRingCapacity(Math.addExact(size(), values.length));
        for (boolean value : values) {
            enqueue(value);
        }
    }

    /**
     * Removes and returns the head element.
     *
     * @return the removed element
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public boolean dequeue()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        long index = headIndex();
        boolean value = elementsUnchecked().getUnchecked(index);
        advanceHead();
        return value;
    }

    /**
     * Reads the head element without removing it.
     *
     * @return the head element
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public boolean peek()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return elementsUnchecked().getUnchecked(headIndex());
    }
}
