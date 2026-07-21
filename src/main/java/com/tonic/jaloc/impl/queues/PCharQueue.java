package com.tonic.jaloc.impl.queues;

import com.tonic.jaloc.impl.arrays.PCharArray;
import com.tonic.jaloc.impl.arrays.PCharWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveQueue;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

/**
 * A growable native char FIFO queue on a ring.
 */
public final class PCharQueue extends AbstractPrimitiveQueue<PCharArray, PCharWriter> {
    /**
     * Creates an empty queue with zero capacity on the system allocator.
     */
    public PCharQueue() {
        this(0);
    }

    /**
     * Creates an empty queue with the given starting capacity on the system allocator.
     *
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PCharQueue(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    /**
     * Creates an empty queue with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PCharQueue(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PCharArray(allocator, initialCapacity));
    }

    @Override
    protected PCharArray createArray(NativeAllocator allocator, long capacity) {
        return new PCharArray(allocator, capacity);
    }

    /**
     * Grows capacity to at least requiredCapacity.
     *
     * @param requiredCapacity the minimum capacity
     * @throws IllegalArgumentException if requiredCapacity is negative
     * @throws IllegalStateException if closed
     */
    public void ensureCapacity(long requiredCapacity) {
        ensureRingCapacity(requiredCapacity);
    }

    /**
     * Shrinks capacity to the current size.
     *
     * @throws IllegalStateException if closed
     */
    public void trimToSize() {
        trimRing();
    }

    /**
     * Enqueues value at the tail, growing if needed.
     *
     * @param value the value to enqueue
     * @throws IllegalStateException if closed
     */
    public void enqueue(char value) {
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
    public void enqueueAll(char... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        ensureRingCapacity(Math.addExact(size(), values.length));

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
