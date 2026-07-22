package com.tonic.jaloc.impl.ringbuffers;

import com.tonic.jaloc.impl.arrays.PFloatArray;
import com.tonic.jaloc.impl.arrays.PFloatWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveRingBuffer;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

/**
 * A fixed-capacity native float ring buffer; enqueueing when full overwrites the oldest element.
 */
public final class PFloatRingBuffer extends AbstractPrimitiveRingBuffer<PFloatArray, PFloatWriter> {
    /**
     * Allocates a buffer of the given capacity on the system allocator.
     *
     * @param capacity the fixed capacity
     * @throws IllegalArgumentException if capacity is not positive
     */
    public PFloatRingBuffer(long capacity) {
        this(SystemAllocator.getInstance(), capacity);
    }

    /**
     * Allocates a buffer of the given capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param capacity the fixed capacity
     * @throws IllegalArgumentException if capacity is not positive
     */
    public PFloatRingBuffer(NativeAllocator allocator, long capacity) {
        super(allocator, new PFloatArray(allocator, requireCapacity(capacity)));
    }

    @Override
    protected PFloatArray createArray(NativeAllocator allocator, long capacity) {
        return new PFloatArray(allocator, capacity);
    }

    /**
     * Enqueues value at the tail, overwriting the oldest element when full.
     *
     * @param value the value to enqueue
     * @throws IllegalStateException if closed
     */
    public void enqueue(float value) {
        if (size() == capacity()) {
            long index = headIndex();
            elementsUnchecked().setUnchecked(index, value);
            rotateHead();
            return;
        }

        long index = reserveTail();
        elementsUnchecked().setUnchecked(index, value);
        commitTail();
    }

    /**
     * Enqueues values left to right; only the last capacity values survive overflow.
     *
     * @param values the values to enqueue
     * @throws NullPointerException if values is null
     * @throws IllegalStateException if closed
     */
    public void enqueueAll(float... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        for (float value : values) {
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
    public float dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Ring buffer is empty");
        }
        long index = headIndex();
        float value = elementsUnchecked().getUnchecked(index);
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
    public float peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Ring buffer is empty");
        }
        return elementsUnchecked().getUnchecked(headIndex());
    }

}
