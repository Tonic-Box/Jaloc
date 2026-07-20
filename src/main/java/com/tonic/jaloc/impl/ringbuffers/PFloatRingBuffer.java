package com.tonic.jaloc.impl.ringbuffers;

import com.tonic.jaloc.impl.arrays.PFloatArray;
import com.tonic.jaloc.impl.arrays.PFloatWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveRingBuffer;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PFloatRingBuffer extends AbstractPrimitiveRingBuffer<PFloatArray, PFloatWriter> {
    public PFloatRingBuffer(long capacity) {
        this(SystemAllocator.getInstance(), capacity);
    }

    public PFloatRingBuffer(NativeAllocator allocator, long capacity) {
        super(allocator, new PFloatArray(allocator, requireCapacity(capacity)));
    }

    @Override
    protected PFloatArray createArray(NativeAllocator allocator, long capacity) {
        return new PFloatArray(allocator, capacity);
    }

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

    public void enqueueAll(float... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        for (float value : values) {
            enqueue(value);
        }
    }

    public float dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Ring buffer is empty");
        }
        long index = headIndex();
        float value = elementsUnchecked().getUnchecked(index);
        advanceHead();
        return value;
    }

    public float peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Ring buffer is empty");
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
