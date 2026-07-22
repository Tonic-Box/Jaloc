package com.tonic.jaloc.impl.fixedqueues;

import com.tonic.jaloc.impl.arrays.PIntArray;
import com.tonic.jaloc.impl.arrays.PIntWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveFixedQueue;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

/**
 * A fixed-capacity native int FIFO queue that rejects when full.
 */
public final class PIntFixedQueue extends AbstractPrimitiveFixedQueue<PIntArray, PIntWriter> {
    /**
     * Allocates a queue of the given capacity on the system allocator.
     *
     * @param capacity the fixed capacity
     * @throws IllegalArgumentException if capacity is not positive
     */
    public PIntFixedQueue(long capacity) {
        this(SystemAllocator.getInstance(), capacity);
    }

    /**
     * Allocates a queue of the given capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param capacity the fixed capacity
     * @throws IllegalArgumentException if capacity is not positive
     */
    public PIntFixedQueue(NativeAllocator allocator, long capacity) {
        super(allocator, new PIntArray(allocator, requireCapacity(capacity)));
    }

    @Override
    protected PIntArray createArray(NativeAllocator allocator, long capacity) {
        return new PIntArray(allocator, capacity);
    }

    /**
     * Enqueues value at the tail.
     *
     * @param value the value to enqueue
     * @throws IllegalStateException if full or closed
     */
    public void enqueue(int value) {
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
    public boolean offer(int value) {
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
    public void enqueueAll(int... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        if (Math.addExact(size(), values.length) > capacity()) {
            throw new IllegalStateException("Queue is full");
        }

        long start = physicalIndex(sizeUnchecked());
        PIntArray table = elementsUnchecked();
        long firstSegment = Math.min(values.length, table.length() - start);

        table.copyFrom(values, 0, start, (int) firstSegment);

        if (firstSegment < values.length) {
            table.copyFrom(values, (int) firstSegment, 0, values.length - (int) firstSegment);
        }

        size(sizeUnchecked() + values.length);
    }

    /**
     * Removes and returns the head element.
     *
     * @return the removed element
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public int dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        long index = headIndex();
        int value = elementsUnchecked().getUnchecked(index);
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
    public int peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return elementsUnchecked().getUnchecked(headIndex());
    }

}
