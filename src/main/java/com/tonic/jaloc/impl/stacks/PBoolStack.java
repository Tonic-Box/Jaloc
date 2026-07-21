package com.tonic.jaloc.impl.stacks;

import com.tonic.jaloc.impl.arrays.PBoolArray;
import com.tonic.jaloc.impl.arrays.PBoolWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveStack;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A growable native bool stack over a bit-packed array.
 */
public final class PBoolStack extends AbstractPrimitiveStack<PBoolArray, PBoolWriter>
{
    /**
     * Creates an empty stack with zero capacity on the system allocator.
     */
    public PBoolStack()
    {
        this(0);
    }

    /**
     * Creates an empty stack with the given starting capacity on the system allocator.
     *
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PBoolStack(long initialCapacity)
    {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    /**
     * Creates an empty stack with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PBoolStack(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PBoolArray(allocator, initialCapacity));
    }

    @Override
    protected PBoolArray createArray(NativeAllocator allocator, long capacity) {
        return new PBoolArray(allocator, capacity);
    }

    /**
     * Pushes value on top, growing if needed.
     *
     * @param value the value to push
     * @throws IllegalStateException if closed
     */
    public void push(boolean value)
    {
        long s = appendIndex();
        elementsUnchecked().setUnchecked(s, value);
        size(s + 1);
    }

    /**
     * Pushes values left to right; the last one ends up on top.
     *
     * @param values the values to push
     * @throws NullPointerException if values is null
     * @throws IllegalStateException if closed
     */
    public void pushAll(boolean... values)
    {
        Objects.requireNonNull(values, "values");
        if (values.length == 0) {
            return;
        }
        PBoolWriter writer = appendWriter(values.length);
        for (boolean value : values) {
            writer.put(value);
        }
        commitWriter();
    }

    /**
     * Removes and returns the top element.
     *
     * @return the removed element
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public boolean pop()
    {
        ensureOpen();
        long s = sizeUnchecked();
        if (s == 0) {
            throw new NoSuchElementException("Stack is empty");
        }
        boolean value = elementsUnchecked().getUnchecked(s - 1);
        elementsUnchecked().setUnchecked(s - 1, false);
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
    public boolean peek()
    {
        ensureOpen();
        long s = sizeUnchecked();
        if (s == 0) {
            throw new NoSuchElementException("Stack is empty");
        }
        return elementsUnchecked().getUnchecked(s - 1);
    }
}
