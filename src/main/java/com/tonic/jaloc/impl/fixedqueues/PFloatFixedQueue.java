package com.tonic.jaloc.impl.fixedqueues;

import com.tonic.jaloc.impl.arrays.PFloatArray;
import com.tonic.jaloc.impl.arrays.PFloatWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveFixedQueue;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PFloatFixedQueue extends AbstractPrimitiveFixedQueue<PFloatArray, PFloatWriter> {
    public PFloatFixedQueue(long capacity) {
        this(SystemAllocator.getInstance(), capacity);
    }

    public PFloatFixedQueue(NativeAllocator allocator, long capacity) {
        super(allocator, new PFloatArray(allocator, requireCapacity(capacity)));
    }

    @Override
    protected PFloatArray createArray(NativeAllocator allocator, long capacity) {
        return new PFloatArray(allocator, capacity);
    }

    public void enqueue(float value) {
        if (size() == capacity()) {
            throw new IllegalStateException("Queue is full");
        }

        long index = reserveTail();
        elements().set(index, value);
        commitTail();
    }

    public boolean offer(float value) {
        if (size() == capacity()) {
            return false;
        }

        long index = reserveTail();
        elements().set(index, value);
        commitTail();
        return true;
    }

    public void enqueueAll(float... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        if (Math.addExact(size(), values.length) > capacity()) {
            throw new IllegalStateException("Queue is full");
        }

        for (float value : values) {
            long index = reserveTail();
            elements().set(index, value);
            commitTail();
        }
    }

    public float dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        long index = headIndex();
        float value = elements().get(index);
        advanceHead();
        return value;
    }

    public float peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return elements().get(headIndex());
    }

    private static long requireCapacity(long capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        return capacity;
    }
}
