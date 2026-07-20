package com.tonic.jaloc.impl.deques;

import com.tonic.jaloc.impl.arrays.PByteArray;
import com.tonic.jaloc.impl.arrays.PByteWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveDeque;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PByteDeque extends AbstractPrimitiveDeque<PByteArray, PByteWriter> {
    public PByteDeque() {
        this(0);
    }

    public PByteDeque(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PByteDeque(NativeAllocator allocator, long initialCapacity) {
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

    public void addFirst(byte value) {
        long index = reserveHead();
        elements().set(index, value);
        commitHead();
    }

    public void addLast(byte value) {
        long index = reserveTail();
        elements().set(index, value);
        commitTail();
    }

    public byte removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = headIndex();
        byte value = elements().get(index);
        advanceHead();
        return value;
    }

    public byte removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = tailIndex();
        byte value = elements().get(index);
        shrinkTail();
        return value;
    }

    public byte peekFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elements().get(headIndex());
    }

    public byte peekLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elements().get(tailIndex());
    }
}
