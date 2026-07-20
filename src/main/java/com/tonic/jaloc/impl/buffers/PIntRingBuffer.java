package com.tonic.jaloc.impl.buffers;

import com.tonic.jaloc.impl.arrays.PIntArray;
import com.tonic.jaloc.impl.arrays.PIntWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveRingBuffer;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PIntRingBuffer extends AbstractPrimitiveRingBuffer<PIntArray, PIntWriter> {
    public PIntRingBuffer(long capacity) {
        this(SystemAllocator.getInstance(), capacity);
    }

    public PIntRingBuffer(NativeAllocator allocator, long capacity) {
        super(allocator, new PIntArray(allocator, requireCapacity(capacity)));
    }

    @Override
    protected PIntArray createArray(NativeAllocator allocator, long capacity) {
        return new PIntArray(allocator, capacity);
    }

    public void enqueue(int value) {
        if (size() == capacity()) {
            long index = headIndex();
            elements().set(index, value);
            rotateHead();
            return;
        }

        long index = reserveTail();
        elements().set(index, value);
        commitTail();
    }

    public void enqueueAll(int... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        for (int value : values) {
            enqueue(value);
        }
    }

    public int dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Ring buffer is empty");
        }
        long index = headIndex();
        int value = elements().get(index);
        advanceHead();
        return value;
    }

    public int peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Ring buffer is empty");
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
