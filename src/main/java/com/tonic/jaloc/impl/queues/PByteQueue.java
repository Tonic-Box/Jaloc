package com.tonic.jaloc.impl.queues;

import com.tonic.jaloc.impl.arrays.PByteArray;
import com.tonic.jaloc.impl.arrays.PByteWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveQueue;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PByteQueue extends AbstractPrimitiveQueue<PByteArray, PByteWriter> {
    public PByteQueue() {
        this(0);
    }

    public PByteQueue(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PByteQueue(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PByteArray(allocator, initialCapacity));
    }

    @Override
    protected PByteArray createArray(NativeAllocator allocator, long capacity) {
        return new PByteArray(allocator, capacity);
    }

    public void ensureCapacity(long requiredCapacity) {
        ensureRingCapacity(requiredCapacity);
    }

    public void trimToSize() {
        trimRing();
    }

    public void enqueue(byte value) {
        long index = reserveTail();
        elements().set(index, value);
        commitTail();
    }

    public void enqueueAll(byte... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        ensureRingCapacity(Math.addExact(size(), values.length));

        for (byte value : values) {
            enqueue(value);
        }
    }

    public byte dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        long index = headIndex();
        byte value = elements().get(index);
        advanceHead();
        return value;
    }

    public byte peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return elements().get(headIndex());
    }
}
