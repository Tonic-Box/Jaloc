package com.tonic.jaloc.impl.ringbuffers;

import com.tonic.jaloc.impl.arrays.PDoubleArray;
import com.tonic.jaloc.impl.arrays.PDoubleWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveRingBuffer;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PDoubleRingBuffer extends AbstractPrimitiveRingBuffer<PDoubleArray, PDoubleWriter> {
    public PDoubleRingBuffer(long capacity) {
        this(SystemAllocator.getInstance(), capacity);
    }

    public PDoubleRingBuffer(NativeAllocator allocator, long capacity) {
        super(allocator, new PDoubleArray(allocator, requireCapacity(capacity)));
    }

    @Override
    protected PDoubleArray createArray(NativeAllocator allocator, long capacity) {
        return new PDoubleArray(allocator, capacity);
    }

    public void enqueue(double value) {
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

    public void enqueueAll(double... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        for (double value : values) {
            enqueue(value);
        }
    }

    public double dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Ring buffer is empty");
        }
        long index = headIndex();
        double value = elementsUnchecked().getUnchecked(index);
        advanceHead();
        return value;
    }

    public double peek() {
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
