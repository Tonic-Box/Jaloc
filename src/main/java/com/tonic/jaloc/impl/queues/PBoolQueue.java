package com.tonic.jaloc.impl.queues;

import com.tonic.jaloc.impl.arrays.PBoolArray;
import com.tonic.jaloc.impl.arrays.PBoolWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveQueue;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;
import java.util.Objects;

public final class PBoolQueue extends AbstractPrimitiveQueue<PBoolArray, PBoolWriter>
{
    public PBoolQueue()
    {
        this(0);
    }

    public PBoolQueue(long initialCapacity)
    {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PBoolQueue(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PBoolArray(allocator, initialCapacity));
    }

    @Override
    protected PBoolArray createArray(NativeAllocator allocator, long capacity) {
        return new PBoolArray(allocator, capacity);
    }

    @Override
    protected void migrateElements(PBoolArray source, PBoolArray destination)
    {
        long size = size();

        for (long i = 0; i < size; i++)
        {
            destination.set(i, source.get(physicalIndex(i)));
        }
    }

    public void ensureCapacity(long requiredCapacity)
    {
        ensureRingCapacity(requiredCapacity);
    }

    public void trimToSize()
    {
        trimRing();
    }

    public void enqueue(boolean value)
    {
        long index = reserveTail();
        elementsUnchecked().setUnchecked(index, value);
        commitTail();
    }

    public void enqueueAll(boolean... values)
    {
        Objects.requireNonNull(values, "values");
        if (values.length == 0) {
            return;
        }
        ensureRingCapacity(Math.addExact(size(), values.length));
        for (boolean value : values) {
            enqueue(value);
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
}
