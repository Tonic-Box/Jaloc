package com.tonic.jaloc.impl.buffers;

import com.tonic.jaloc.impl.arrays.PByteArray;
import com.tonic.jaloc.impl.arrays.PByteWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveRingBuffer;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PByteRingBuffer extends AbstractPrimitiveRingBuffer<PByteArray, PByteWriter> {
    public PByteRingBuffer(long capacity) {
        this(SystemAllocator.getInstance(), capacity);
    }

    public PByteRingBuffer(NativeAllocator allocator, long capacity) {
        super(allocator, new PByteArray(allocator, requireCapacity(capacity)));
    }

    @Override
    protected PByteArray createArray(NativeAllocator allocator, long capacity) {
        return new PByteArray(allocator, capacity);
    }

    public void enqueue(byte value) {
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

    public void enqueueAll(byte... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        for (byte value : values) {
            enqueue(value);
        }
    }

    public byte dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Ring buffer is empty");
        }
        long index = headIndex();
        byte value = elements().get(index);
        advanceHead();
        return value;
    }

    public byte peek() {
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
