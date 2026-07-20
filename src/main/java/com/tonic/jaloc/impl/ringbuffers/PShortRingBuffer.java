package com.tonic.jaloc.impl.ringbuffers;

import com.tonic.jaloc.impl.arrays.PShortArray;
import com.tonic.jaloc.impl.arrays.PShortWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveRingBuffer;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PShortRingBuffer extends AbstractPrimitiveRingBuffer<PShortArray, PShortWriter> {
    public PShortRingBuffer(long capacity) {
        this(SystemAllocator.getInstance(), capacity);
    }

    public PShortRingBuffer(NativeAllocator allocator, long capacity) {
        super(allocator, new PShortArray(allocator, requireCapacity(capacity)));
    }

    @Override
    protected PShortArray createArray(NativeAllocator allocator, long capacity) {
        return new PShortArray(allocator, capacity);
    }

    public void enqueue(short value) {
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

    public void enqueueAll(short... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        for (short value : values) {
            enqueue(value);
        }
    }

    public short dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Ring buffer is empty");
        }
        long index = headIndex();
        short value = elementsUnchecked().getUnchecked(index);
        advanceHead();
        return value;
    }

    public short peek() {
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
