package com.tonic.jaloc.impl.fixedqueues;

import com.tonic.jaloc.impl.arrays.PBoolArray;
import com.tonic.jaloc.impl.arrays.PBoolWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveFixedQueue;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;
import java.util.Objects;

public final class PBoolFixedQueue extends AbstractPrimitiveFixedQueue<PBoolArray, PBoolWriter>
{
    public PBoolFixedQueue(long capacity)
    {
        this(SystemAllocator.getInstance(), capacity);
    }

    public PBoolFixedQueue(NativeAllocator allocator, long capacity) {
        super(allocator, new PBoolArray(allocator, requireCapacity(capacity)));
    }

    @Override
    protected PBoolArray createArray(NativeAllocator allocator, long capacity) {
        return new PBoolArray(allocator, capacity);
    }

    public void enqueue(boolean value)
    {
        if (size() == capacity()) {
            throw new IllegalStateException("Queue is full");
        }

        long index = reserveTail();
        elementsUnchecked().setUnchecked(index, value);
        commitTail();
    }

    public boolean offer(boolean value)
    {
        if (size() == capacity()) {
            return false;
        }

        long index = reserveTail();
        elementsUnchecked().setUnchecked(index, value);
        commitTail();
        return true;
    }

    public void enqueueAll(boolean... values)
    {
        Objects.requireNonNull(values, "values");
        if (values.length == 0) {
            return;
        }
        if (Math.addExact(size(), (long) values.length) > capacity()) {
            throw new IllegalStateException("Queue is full");
        }
        for (boolean value : values) {
            long index = reserveTail();
            elementsUnchecked().setUnchecked(index, value);
            commitTail();
        }
    }

    public boolean dequeue()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
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
            throw new NoSuchElementException("Queue is empty");
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
