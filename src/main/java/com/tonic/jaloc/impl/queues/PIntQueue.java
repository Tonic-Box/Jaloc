package com.tonic.jaloc.impl.queues;

import com.tonic.jaloc.impl.arrays.PIntArray;
import com.tonic.jaloc.impl.arrays.PIntWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveQueue;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PIntQueue extends AbstractPrimitiveQueue<PIntArray, PIntWriter> {
    public PIntQueue() {
        this(0);
    }

    public PIntQueue(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PIntQueue(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PIntArray(allocator, initialCapacity));
    }

    @Override
    protected PIntArray createArray(NativeAllocator allocator, long capacity) {
        return new PIntArray(allocator, capacity);
    }

    public void ensureCapacity(long requiredCapacity) {
        ensureRingCapacity(requiredCapacity);
    }

    public void trimToSize() {
        trimRing();
    }

    public void enqueue(int value) {
        long index = reserveTail();
        elements().set(index, value);
        commitTail();
    }

    public void enqueueAll(int... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        ensureRingCapacity(Math.addExact(size(), values.length));

        for (int value : values) {
            enqueue(value);
        }
    }

    public int dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        long index = headIndex();
        int value = elements().get(index);
        advanceHead();
        return value;
    }

    public int peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return elements().get(headIndex());
    }
}
