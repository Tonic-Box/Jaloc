package com.tonic.jaloc.impl.fixedqueues;

import com.tonic.jaloc.impl.arrays.PByteArray;
import com.tonic.jaloc.impl.arrays.PByteWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveFixedQueue;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PByteFixedQueue extends AbstractPrimitiveFixedQueue<PByteArray, PByteWriter> {
    public PByteFixedQueue(long capacity) {
        this(SystemAllocator.getInstance(), capacity);
    }

    public PByteFixedQueue(NativeAllocator allocator, long capacity) {
        super(allocator, new PByteArray(allocator, requireCapacity(capacity)));
    }

    @Override
    protected PByteArray createArray(NativeAllocator allocator, long capacity) {
        return new PByteArray(allocator, capacity);
    }

    public void enqueue(byte value) {
        if (size() == capacity()) {
            throw new IllegalStateException("Queue is full");
        }

        long index = reserveTail();
        elementsUnchecked().setUnchecked(index, value);
        commitTail();
    }

    public boolean offer(byte value) {
        if (size() == capacity()) {
            return false;
        }

        long index = reserveTail();
        elementsUnchecked().setUnchecked(index, value);
        commitTail();
        return true;
    }

    public void enqueueAll(byte... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        if (Math.addExact(size(), values.length) > capacity()) {
            throw new IllegalStateException("Queue is full");
        }

        for (byte value : values) {
            long index = reserveTail();
            elementsUnchecked().setUnchecked(index, value);
            commitTail();
        }
    }

    public byte dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        long index = headIndex();
        byte value = elementsUnchecked().getUnchecked(index);
        advanceHead();
        return value;
    }

    public byte peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return elementsUnchecked().getUnchecked(headIndex());
    }

    private static long requireCapacity(long capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        return capacity;
    }
}
