package com.tonic.jaloc.impl.fixedqueues;

import com.tonic.jaloc.impl.arrays.PShortArray;
import com.tonic.jaloc.impl.arrays.PShortWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveFixedQueue;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PShortFixedQueue extends AbstractPrimitiveFixedQueue<PShortArray, PShortWriter> {
    public PShortFixedQueue(long capacity) {
        this(SystemAllocator.getInstance(), capacity);
    }

    public PShortFixedQueue(NativeAllocator allocator, long capacity) {
        super(allocator, new PShortArray(allocator, requireCapacity(capacity)));
    }

    @Override
    protected PShortArray createArray(NativeAllocator allocator, long capacity) {
        return new PShortArray(allocator, capacity);
    }

    public void enqueue(short value) {
        if (size() == capacity()) {
            throw new IllegalStateException("Queue is full");
        }

        long index = reserveTail();
        elements().set(index, value);
        commitTail();
    }

    public boolean offer(short value) {
        if (size() == capacity()) {
            return false;
        }

        long index = reserveTail();
        elements().set(index, value);
        commitTail();
        return true;
    }

    public void enqueueAll(short... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        if (Math.addExact(size(), values.length) > capacity()) {
            throw new IllegalStateException("Queue is full");
        }

        for (short value : values) {
            long index = reserveTail();
            elements().set(index, value);
            commitTail();
        }
    }

    public short dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        long index = headIndex();
        short value = elements().get(index);
        advanceHead();
        return value;
    }

    public short peek() {
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
