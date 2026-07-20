package com.tonic.jaloc.impl.queues;

import com.tonic.jaloc.impl.arrays.PLongArray;
import com.tonic.jaloc.impl.arrays.PLongWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveQueue;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PLongQueue extends AbstractPrimitiveQueue<PLongArray, PLongWriter> {
    public PLongQueue() {
        this(0);
    }

    public PLongQueue(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PLongQueue(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PLongArray(allocator, initialCapacity));
    }

    @Override
    protected PLongArray createArray(NativeAllocator allocator, long capacity) {
        return new PLongArray(allocator, capacity);
    }

    public void ensureCapacity(long requiredCapacity) {
        ensureRingCapacity(requiredCapacity);
    }

    public void trimToSize() {
        trimRing();
    }

    public void enqueue(long value) {
        long index = reserveTail();
        elements().set(index, value);
        commitTail();
    }

    public void enqueueAll(long... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        ensureRingCapacity(Math.addExact(size(), values.length));

        for (long value : values) {
            enqueue(value);
        }
    }

    public long dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        long index = headIndex();
        long value = elements().get(index);
        advanceHead();
        return value;
    }

    public long peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return elements().get(headIndex());
    }
}
