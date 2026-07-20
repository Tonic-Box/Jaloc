package com.tonic.jaloc.impl.fixedqueues;

import com.tonic.jaloc.impl.arrays.PLongArray;
import com.tonic.jaloc.impl.arrays.PLongWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveFixedQueue;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PLongFixedQueue extends AbstractPrimitiveFixedQueue<PLongArray, PLongWriter> {
    public PLongFixedQueue(long capacity) {
        this(SystemAllocator.getInstance(), capacity);
    }

    public PLongFixedQueue(NativeAllocator allocator, long capacity) {
        super(allocator, new PLongArray(allocator, requireCapacity(capacity)));
    }

    @Override
    protected PLongArray createArray(NativeAllocator allocator, long capacity) {
        return new PLongArray(allocator, capacity);
    }

    public void enqueue(long value) {
        if (size() == capacity()) {
            throw new IllegalStateException("Queue is full");
        }

        long index = reserveTail();
        elements().set(index, value);
        commitTail();
    }

    public boolean offer(long value) {
        if (size() == capacity()) {
            return false;
        }

        long index = reserveTail();
        elements().set(index, value);
        commitTail();
        return true;
    }

    public void enqueueAll(long... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        if (Math.addExact(size(), values.length) > capacity()) {
            throw new IllegalStateException("Queue is full");
        }

        for (long value : values) {
            long index = reserveTail();
            elements().set(index, value);
            commitTail();
        }
    }

    public long dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        long index = headIndex();
        long value = elements().get(index);
        advanceHead();
        return value;
    }

    public long peek() {
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
