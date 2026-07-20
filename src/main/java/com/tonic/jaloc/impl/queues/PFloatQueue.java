package com.tonic.jaloc.impl.queues;

import com.tonic.jaloc.impl.arrays.PFloatArray;
import com.tonic.jaloc.impl.arrays.PFloatWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveQueue;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PFloatQueue extends AbstractPrimitiveQueue<PFloatArray, PFloatWriter> {
    public PFloatQueue() {
        this(0);
    }

    public PFloatQueue(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PFloatQueue(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PFloatArray(allocator, initialCapacity));
    }

    @Override
    protected PFloatArray createArray(NativeAllocator allocator, long capacity) {
        return new PFloatArray(allocator, capacity);
    }

    public void ensureCapacity(long requiredCapacity) {
        ensureRingCapacity(requiredCapacity);
    }

    public void trimToSize() {
        trimRing();
    }

    public void enqueue(float value) {
        long index = reserveTail();
        elements().set(index, value);
        commitTail();
    }

    public void enqueueAll(float... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        ensureRingCapacity(Math.addExact(size(), values.length));

        for (float value : values) {
            enqueue(value);
        }
    }

    public float dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        long index = headIndex();
        float value = elements().get(index);
        advanceHead();
        return value;
    }

    public float peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return elements().get(headIndex());
    }
}
