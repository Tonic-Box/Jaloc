package com.tonic.jaloc.impl.fixedqueues;

import com.tonic.jaloc.impl.arrays.PCharArray;
import com.tonic.jaloc.impl.arrays.PCharWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveFixedQueue;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PCharFixedQueue extends AbstractPrimitiveFixedQueue<PCharArray, PCharWriter> {
    public PCharFixedQueue(long capacity) {
        this(SystemAllocator.getInstance(), capacity);
    }

    public PCharFixedQueue(NativeAllocator allocator, long capacity) {
        super(allocator, new PCharArray(allocator, requireCapacity(capacity)));
    }

    @Override
    protected PCharArray createArray(NativeAllocator allocator, long capacity) {
        return new PCharArray(allocator, capacity);
    }

    public void enqueue(char value) {
        if (size() == capacity()) {
            throw new IllegalStateException("Queue is full");
        }

        long index = reserveTail();
        elements().set(index, value);
        commitTail();
    }

    public boolean offer(char value) {
        if (size() == capacity()) {
            return false;
        }

        long index = reserveTail();
        elements().set(index, value);
        commitTail();
        return true;
    }

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

        for (char value : values) {
            long index = reserveTail();
            elements().set(index, value);
            commitTail();
        }
    }

    public char dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        long index = headIndex();
        char value = elements().get(index);
        advanceHead();
        return value;
    }

    public char peek() {
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
