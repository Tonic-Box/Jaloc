package com.tonic.jaloc.impl.queues;

import com.tonic.jaloc.impl.arrays.PDoubleArray;
import com.tonic.jaloc.impl.arrays.PDoubleWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveQueue;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PDoubleQueue extends AbstractPrimitiveQueue<PDoubleArray, PDoubleWriter> {
    public PDoubleQueue() {
        this(0);
    }

    public PDoubleQueue(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PDoubleQueue(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PDoubleArray(allocator, initialCapacity));
    }

    @Override
    protected PDoubleArray createArray(NativeAllocator allocator, long capacity) {
        return new PDoubleArray(allocator, capacity);
    }

    public void ensureCapacity(long requiredCapacity) {
        ensureRingCapacity(requiredCapacity);
    }

    public void trimToSize() {
        trimRing();
    }

    public void enqueue(double value) {
        long index = reserveTail();
        elements().set(index, value);
        commitTail();
    }

    public void enqueueAll(double... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        ensureRingCapacity(Math.addExact(size(), values.length));

        for (double value : values) {
            enqueue(value);
        }
    }

    public double dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        long index = headIndex();
        double value = elements().get(index);
        advanceHead();
        return value;
    }

    public double peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return elements().get(headIndex());
    }
}
