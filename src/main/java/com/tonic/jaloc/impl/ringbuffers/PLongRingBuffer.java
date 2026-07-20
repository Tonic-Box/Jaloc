package com.tonic.jaloc.impl.ringbuffers;

import com.tonic.jaloc.impl.arrays.PLongArray;
import com.tonic.jaloc.impl.arrays.PLongWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveRingBuffer;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PLongRingBuffer extends AbstractPrimitiveRingBuffer<PLongArray, PLongWriter> {
    public PLongRingBuffer(long capacity) {
        this(SystemAllocator.getInstance(), capacity);
    }

    public PLongRingBuffer(NativeAllocator allocator, long capacity) {
        super(allocator, new PLongArray(allocator, requireCapacity(capacity)));
    }

    @Override
    protected PLongArray createArray(NativeAllocator allocator, long capacity) {
        return new PLongArray(allocator, capacity);
    }

    public void enqueue(long value) {
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

    public void enqueueAll(long... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        for (long value : values) {
            enqueue(value);
        }
    }

    public long dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Ring buffer is empty");
        }
        long index = headIndex();
        long value = elementsUnchecked().getUnchecked(index);
        advanceHead();
        return value;
    }

    public long peek() {
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
