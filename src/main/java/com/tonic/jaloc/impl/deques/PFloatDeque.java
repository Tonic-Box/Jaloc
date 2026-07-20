package com.tonic.jaloc.impl.deques;

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
        elements().set(index, value);
        commitHead();
    }

    public void addLast(float value) {
        long index = reserveTail();
        elements().set(index, value);
        commitTail();
    }

    public float removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = headIndex();
        float value = elements().get(index);
        advanceHead();
        return value;
    }

    public float removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = tailIndex();
        float value = elements().get(index);
        shrinkTail();
        return value;
    }

    public float peekFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elements().get(headIndex());
    }

    public float peekLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elements().get(tailIndex());
    }
}
