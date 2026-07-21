package com.tonic.jaloc.impl.fixedqueues;

import com.tonic.jaloc.impl.arrays.PBoolArray;
import com.tonic.jaloc.impl.arrays.PBoolWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveFixedQueue;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A fixed-capacity native bool FIFO queue that rejects when full.
 */
public final class PBoolFixedQueue extends AbstractPrimitiveFixedQueue<PBoolArray, PBoolWriter>
{
    /**
     * Allocates a queue of the given capacity on the system allocator.
     *
     * @param capacity the fixed capacity
     * @throws IllegalArgumentException if capacity is not positive
     */
    public PBoolFixedQueue(long capacity)
    {
        this(SystemAllocator.getInstance(), capacity);
    }

    /**
     * Allocates a queue of the given capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param capacity the fixed capacity
     * @throws IllegalArgumentException if capacity is not positive
     */
    public PBoolFixedQueue(NativeAllocator allocator, long capacity) {
        super(allocator, new PBoolArray(allocator, requireCapacity(capacity)));
    }

    @Override
    protected PBoolArray createArray(NativeAllocator allocator, long capacity) {
        return new PBoolArray(allocator, capacity);
    }

    /**
     * Enqueues value at the tail.
     *
     * @param value the value to enqueue
     * @throws IllegalStateException if full or closed
     */
    public void enqueue(boolean value)
    {
        if (size() == capacity()) {
            throw new IllegalStateException("Queue is full");
        }

        long index = reserveTail();
        elementsUnchecked().setUnchecked(index, value);
        commitTail();
    }

    /**
     * Enqueues value at the tail if room remains.
     *
     * @param value the value to enqueue
     * @return true if accepted, false if full
     * @throws IllegalStateException if closed
     */
    public boolean offer(boolean value)
    {
        if (size() == capacity()) {
            return false;
        }

        long index = reserveTail();
        elementsUnchecked().setUnchecked(index, value);
        commitTail();
        return true;
    }

    /**
     * Enqueues values left to right, all or nothing.
     *
     * @param values the values to enqueue
     * @throws NullPointerException if values is null
     * @throws IllegalStateException if the values do not all fit, or if closed
     */
    public void enqueueAll(boolean... values)
    {
        Objects.requireNonNull(values, "values");
        if (values.length == 0) {
            return;
        }
        if (Math.addExact(size(), (long) values.length) > capacity()) {
            throw new IllegalStateException("Queue is full");
        }
        for (boolean value : values) {
            long index = reserveTail();
            elementsUnchecked().setUnchecked(index, value);
            commitTail();
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

    private static long requireCapacity(long capacity)
    {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        return capacity;
    }
}
