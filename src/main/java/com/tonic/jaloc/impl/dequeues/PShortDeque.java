package com.tonic.jaloc.impl.dequeues;

import com.tonic.jaloc.impl.arrays.PShortArray;
import com.tonic.jaloc.impl.arrays.PShortWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveDeque;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PShortDeque extends AbstractPrimitiveDeque<PShortArray, PShortWriter> {
    public PShortDeque() {
        this(0);
    }

    public PShortDeque(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PShortDeque(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PShortArray(allocator, initialCapacity));
    }

    @Override
    protected PShortArray createArray(NativeAllocator allocator, long capacity) {
        return new PShortArray(allocator, capacity);
    }

    public void ensureCapacity(long requiredCapacity) {
        ensureRingCapacity(requiredCapacity);
    }

    public void trimToSize() {
        trimRing();
    }

    public void addFirst(short value) {
        long index = reserveHead();
        elementsUnchecked().setUnchecked(index, value);
        commitHead();
    }

    public void addLast(short value) {
        long index = reserveTail();
        elementsUnchecked().setUnchecked(index, value);
        commitTail();
    }

    public short removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = headIndex();
        short value = elementsUnchecked().getUnchecked(index);
        advanceHead();
        return value;
    }

    public short removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = tailIndex();
        short value = elementsUnchecked().getUnchecked(index);
        shrinkTail();
        return value;
    }

    public short peekFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elementsUnchecked().getUnchecked(headIndex());
    }

    public short peekLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elementsUnchecked().getUnchecked(tailIndex());
    }
}
