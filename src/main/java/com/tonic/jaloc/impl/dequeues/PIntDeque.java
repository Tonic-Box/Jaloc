package com.tonic.jaloc.impl.dequeues;

import com.tonic.jaloc.impl.arrays.PIntArray;
import com.tonic.jaloc.impl.arrays.PIntWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveDeque;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PIntDeque extends AbstractPrimitiveDeque<PIntArray, PIntWriter> {
    public PIntDeque() {
        this(0);
    }

    public PIntDeque(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PIntDeque(NativeAllocator allocator, long initialCapacity) {
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

    public void addFirst(int value) {
        long index = reserveHead();
        elementsUnchecked().setUnchecked(index, value);
        commitHead();
    }

    public void addLast(int value) {
        long index = reserveTail();
        elementsUnchecked().setUnchecked(index, value);
        commitTail();
    }

    public int removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = headIndex();
        int value = elementsUnchecked().getUnchecked(index);
        advanceHead();
        return value;
    }

    public int removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = tailIndex();
        int value = elementsUnchecked().getUnchecked(index);
        shrinkTail();
        return value;
    }

    public int peekFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elementsUnchecked().getUnchecked(headIndex());
    }

    public int peekLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elementsUnchecked().getUnchecked(tailIndex());
    }
}
