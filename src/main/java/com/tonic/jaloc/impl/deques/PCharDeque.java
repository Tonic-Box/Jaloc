package com.tonic.jaloc.impl.deques;

import com.tonic.jaloc.impl.arrays.PCharArray;
import com.tonic.jaloc.impl.arrays.PCharWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveDeque;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PCharDeque extends AbstractPrimitiveDeque<PCharArray, PCharWriter> {
    public PCharDeque() {
        this(0);
    }

    public PCharDeque(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PCharDeque(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PCharArray(allocator, initialCapacity));
    }

    @Override
    protected PCharArray createArray(NativeAllocator allocator, long capacity) {
        return new PCharArray(allocator, capacity);
    }

    public void ensureCapacity(long requiredCapacity) {
        ensureRingCapacity(requiredCapacity);
    }

    public void trimToSize() {
        trimRing();
    }

    public void addFirst(char value) {
        long index = reserveHead();
        elements().set(index, value);
        commitHead();
    }

    public void addLast(char value) {
        long index = reserveTail();
        elements().set(index, value);
        commitTail();
    }

    public char removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = headIndex();
        char value = elements().get(index);
        advanceHead();
        return value;
    }

    public char removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = tailIndex();
        char value = elements().get(index);
        shrinkTail();
        return value;
    }

    public char peekFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elements().get(headIndex());
    }

    public char peekLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elements().get(tailIndex());
    }
}
