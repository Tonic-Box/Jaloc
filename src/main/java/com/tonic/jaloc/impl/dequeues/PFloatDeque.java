package com.tonic.jaloc.impl.dequeues;

import com.tonic.jaloc.impl.arrays.PFloatArray;
import com.tonic.jaloc.impl.arrays.PFloatWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveDeque;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PFloatDeque extends AbstractPrimitiveDeque<PFloatArray, PFloatWriter> {
    public PFloatDeque() {
        this(0);
    }

    public PFloatDeque(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PFloatDeque(NativeAllocator allocator, long initialCapacity) {
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

    public void addFirst(float value) {
        long index = reserveHead();
        elementsUnchecked().setUnchecked(index, value);
        commitHead();
    }

    public void addLast(float value) {
        long index = reserveTail();
        elementsUnchecked().setUnchecked(index, value);
        commitTail();
    }

    public float removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = headIndex();
        float value = elementsUnchecked().getUnchecked(index);
        advanceHead();
        return value;
    }

    public float removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = tailIndex();
        float value = elementsUnchecked().getUnchecked(index);
        shrinkTail();
        return value;
    }

    public float peekFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elementsUnchecked().getUnchecked(headIndex());
    }

    public float peekLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elementsUnchecked().getUnchecked(tailIndex());
    }
}
