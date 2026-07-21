package com.tonic.jaloc.impl.stacks;

import com.tonic.jaloc.impl.arrays.PFloatArray;
import com.tonic.jaloc.impl.arrays.PFloatWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveStack;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

/**
 * A growable native float stack.
 */
public final class PFloatStack extends AbstractPrimitiveStack<PFloatArray, PFloatWriter> {
    /**
     * Creates an empty stack with zero capacity on the system allocator.
     */
    public PFloatStack() {
        this(0);
    }

    /**
     * Creates an empty stack with the given starting capacity on the system allocator.
     *
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PFloatStack(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    /**
     * Creates an empty stack with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PFloatStack(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PFloatArray(allocator, initialCapacity));
    }

    @Override
    protected PFloatArray createArray(NativeAllocator allocator, long capacity) {
        return new PFloatArray(allocator, capacity);
    }

    /**
     * Pushes value on top, growing if needed.
     *
     * @param value the value to push
     * @throws IllegalStateException if closed
     */
    public void push(float value) {
        PFloatWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
    }

    /**
     * Pushes values left to right; the last one ends up on top.
     *
     * @param values the values to push
     * @throws NullPointerException if values is null
     * @throws IllegalStateException if closed
     */
    public void pushAll(float... values) {
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
    public float pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        long lastIndex = size() - 1;
        float value = elementsUnchecked().getUnchecked(lastIndex);
        decrementSize();
        return value;
    }

    /**
     * Reads the top element without removing it.
     *
     * @return the top element
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public float peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        return elementsUnchecked().getUnchecked(size() - 1);
    }
}
