package com.tonic.jaloc.impl.stacks;

import com.tonic.jaloc.impl.arrays.PLongArray;
import com.tonic.jaloc.impl.arrays.PLongWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveStack;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

import java.util.NoSuchElementException;

/**
 * A growable native long stack.
 */
public final class PLongStack extends AbstractPrimitiveStack<PLongArray, PLongWriter> {
    /**
     * Creates an empty stack with zero capacity on the system allocator.
     */
    public PLongStack() {
        this(0);
    }

    /**
     * Creates an empty stack with the given starting capacity on the system allocator.
     *
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PLongStack(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    /**
     * Creates an empty stack with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PLongStack(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PLongArray(allocator, initialCapacity));
    }

    @Override
    protected PLongArray createArray(NativeAllocator allocator, long capacity) {
        return new PLongArray(allocator, capacity);
    }

    /**
     * Pushes value on top, growing if needed.
     *
     * @param value the value to push
     * @throws IllegalStateException if closed
     */
    public void push(long value) {
        long s = appendIndex();
        UnsafeMemory.putLong(elementsBase() + (s << 3), value);
        size(s + 1);
    }

    /**
     * Pushes values left to right; the last one ends up on top.
     *
     * @param values the values to push
     * @throws NullPointerException if values is null
     * @throws IllegalStateException if closed
     */
    public void pushAll(long... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        ensureOpen();

        long s = sizeUnchecked();

        ensureCapacity(Math.addExact(s, values.length));
        elementsUnchecked().copyFrom(values, 0, s, values.length);
        size(s + values.length);
    }

    /**
     * Removes and returns the top element.
     *
     * @return the removed element
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public long pop() {
        ensureOpen();
        long s = sizeUnchecked();
        if (s == 0) {
            throw new NoSuchElementException("Stack is empty");
        }
        long value = UnsafeMemory.getLong(elementsBase() + ((s - 1) << 3));
        size(s - 1);
        return value;
    }

    /**
     * Reads the top element without removing it.
     *
     * @return the top element
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public long peek() {
        ensureOpen();
        long s = sizeUnchecked();
        if (s == 0) {
            throw new NoSuchElementException("Stack is empty");
        }
        return UnsafeMemory.getLong(elementsBase() + ((s - 1) << 3));
    }
}
