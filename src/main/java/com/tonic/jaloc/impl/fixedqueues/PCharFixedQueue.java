package com.tonic.jaloc.impl.fixedqueues;

import com.tonic.jaloc.impl.arrays.PCharArray;
import com.tonic.jaloc.impl.arrays.PCharWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveFixedQueue;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

/**
 * A fixed-capacity native char FIFO queue that rejects when full.
 */
public final class PCharFixedQueue extends AbstractPrimitiveFixedQueue<PCharArray, PCharWriter> {
    /**
     * Allocates a queue of the given capacity on the system allocator.
     *
     * @param capacity the fixed capacity
     * @throws IllegalArgumentException if capacity is not positive
     */
    public PCharFixedQueue(long capacity) {
        this(SystemAllocator.getInstance(), capacity);
    }

    /**
     * Allocates a queue of the given capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param capacity the fixed capacity
     * @throws IllegalArgumentException if capacity is not positive
     */
    public PCharFixedQueue(NativeAllocator allocator, long capacity) {
        super(allocator, new PCharArray(allocator, requireCapacity(capacity)));
    }

    @Override
    protected PCharArray createArray(NativeAllocator allocator, long capacity) {
        return new PCharArray(allocator, capacity);
    }

    /**
     * Enqueues value at the tail.
     *
     * @param value the value to enqueue
     * @throws IllegalStateException if full or closed
     */
    public void enqueue(char value) {
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
    public boolean offer(char value) {
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
    public void enqueueAll(char... values) {
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
        PCharArray table = elementsUnchecked();
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
    public char dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        long index = headIndex();
        char value = elementsUnchecked().getUnchecked(index);
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
    public char peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return elementsUnchecked().getUnchecked(headIndex());
    }

}
