package com.tonic.jaloc.impl.stacks;

import com.tonic.jaloc.impl.arrays.PShortArray;
import com.tonic.jaloc.impl.arrays.PShortWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveStack;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

import java.util.NoSuchElementException;

/**
 * A growable native short stack.
 */
public final class PShortStack extends AbstractPrimitiveStack<PShortArray, PShortWriter> {
    /**
     * Creates an empty stack with zero capacity on the system allocator.
     */
    public PShortStack() {
        this(0);
    }

    /**
     * Creates an empty stack with the given starting capacity on the system allocator.
     *
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PShortStack(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    /**
     * Creates an empty stack with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PShortStack(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PShortArray(allocator, initialCapacity));
    }

    @Override
    protected PShortArray createArray(NativeAllocator allocator, long capacity) {
        return new PShortArray(allocator, capacity);
    }

    /**
     * Pushes value on top, growing if needed.
     *
     * @param value the value to push
     * @throws IllegalStateException if closed
     */
    public void push(short value) {
        long s = appendIndex();
        UnsafeMemory.putShort(elementsBase() + (s << 1), value);
        size(s + 1);
    }

    /**
     * Pushes values left to right; the last one ends up on top.
     *
     * @param values the values to push
     * @throws NullPointerException if values is null
     * @throws IllegalStateException if closed
     */
    public void pushAll(short... values) {
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
    public short pop() {
        ensureOpen();
        long s = sizeUnchecked();
        if (s == 0) {
            throw new NoSuchElementException("Stack is empty");
        }
        short value = UnsafeMemory.getShort(elementsBase() + ((s - 1) << 1));
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
    public short peek() {
        ensureOpen();
        long s = sizeUnchecked();
        if (s == 0) {
            throw new NoSuchElementException("Stack is empty");
        }
        return UnsafeMemory.getShort(elementsBase() + ((s - 1) << 1));
    }
}
