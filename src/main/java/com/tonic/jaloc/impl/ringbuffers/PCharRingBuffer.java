package com.tonic.jaloc.impl.ringbuffers;

import com.tonic.jaloc.impl.arrays.PCharArray;
import com.tonic.jaloc.impl.arrays.PCharWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveRingBuffer;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PCharRingBuffer extends AbstractPrimitiveRingBuffer<PCharArray, PCharWriter> {
    public PCharRingBuffer(long capacity) {
        this(SystemAllocator.getInstance(), capacity);
    }

    public PCharRingBuffer(NativeAllocator allocator, long capacity) {
        super(allocator, new PCharArray(allocator, requireCapacity(capacity)));
    }

    @Override
    protected PCharArray createArray(NativeAllocator allocator, long capacity) {
        return new PCharArray(allocator, capacity);
    }

    public void enqueue(char value) {
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

    public void enqueueAll(char... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        for (char value : values) {
            enqueue(value);
        }
    }

    public char dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Ring buffer is empty");
        }
        long index = headIndex();
        char value = elements().get(index);
        advanceHead();
        return value;
    }

    public char peek() {
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
