package com.tonic.jaloc.impl.deques;

import com.tonic.jaloc.impl.arrays.PLongArray;
import com.tonic.jaloc.impl.arrays.PLongWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveDeque;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PLongDeque extends AbstractPrimitiveDeque<PLongArray, PLongWriter> {
    public PLongDeque() {
        this(0);
    }

    public PLongDeque(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PLongDeque(NativeAllocator allocator, long initialCapacity) {
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

    public void addFirst(long value) {
        long index = reserveHead();
        elements().set(index, value);
        commitHead();
    }

    public void addLast(long value) {
        long index = reserveTail();
        elements().set(index, value);
        commitTail();
    }

    public long removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = headIndex();
        long value = elements().get(index);
        advanceHead();
        return value;
    }

    public long removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = tailIndex();
        long value = elements().get(index);
        shrinkTail();
        return value;
    }

    public long peekFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elements().get(headIndex());
    }

    public long peekLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elements().get(tailIndex());
    }
}
