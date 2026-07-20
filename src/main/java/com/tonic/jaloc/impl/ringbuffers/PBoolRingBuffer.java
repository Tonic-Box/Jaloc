package com.tonic.jaloc.impl.ringbuffers;

import com.tonic.jaloc.impl.arrays.PBoolArray;
import com.tonic.jaloc.impl.arrays.PBoolWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveRingBuffer;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;
import java.util.Objects;

public final class PBoolRingBuffer extends AbstractPrimitiveRingBuffer<PBoolArray, PBoolWriter>
{
    public PBoolRingBuffer(long capacity)
    {
        this(SystemAllocator.getInstance(), capacity);
    }

    public PBoolRingBuffer(NativeAllocator allocator, long capacity) {
        super(allocator, new PBoolArray(allocator, requireCapacity(capacity)));
    }

    @Override
    protected PBoolArray createArray(NativeAllocator allocator, long capacity) {
        return new PBoolArray(allocator, capacity);
    }

    public void enqueue(boolean value)
    {
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

    public void enqueueAll(boolean... values)
    {
        Objects.requireNonNull(values, "values");
        for (boolean value : values) {
            enqueue(value);
        }
    }

    public boolean dequeue()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Ring buffer is empty");
        }
        long index = headIndex();
        boolean value = elementsUnchecked().getUnchecked(index);
        elementsUnchecked().setUnchecked(index, false);
        advanceHead();
        return value;
    }

    public boolean peek()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Ring buffer is empty");
        }
        return elementsUnchecked().getUnchecked(headIndex());
    }

    private static long requireCapacity(long capacity)
    {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        return capacity;
    }
}
