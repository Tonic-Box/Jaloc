package com.tonic.jaloc.impl.dequeues;

import com.tonic.jaloc.impl.arrays.PDoubleArray;
import com.tonic.jaloc.impl.arrays.PDoubleWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveDeque;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PDoubleDeque extends AbstractPrimitiveDeque<PDoubleArray, PDoubleWriter> {
    public PDoubleDeque() {
        this(0);
    }

    public PDoubleDeque(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PDoubleDeque(NativeAllocator allocator, long initialCapacity) {
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

    public void addFirst(double value) {
        long index = reserveHead();
        elementsUnchecked().setUnchecked(index, value);
        commitHead();
    }

    public void addLast(double value) {
        long index = reserveTail();
        elementsUnchecked().setUnchecked(index, value);
        commitTail();
    }

    public double removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = headIndex();
        double value = elementsUnchecked().getUnchecked(index);
        advanceHead();
        return value;
    }

    public double removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = tailIndex();
        double value = elementsUnchecked().getUnchecked(index);
        shrinkTail();
        return value;
    }

    public double peekFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elementsUnchecked().getUnchecked(headIndex());
    }

    public double peekLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elementsUnchecked().getUnchecked(tailIndex());
    }
}
