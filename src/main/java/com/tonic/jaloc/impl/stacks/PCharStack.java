package com.tonic.jaloc.impl.stacks;

import com.tonic.jaloc.impl.arrays.PCharArray;
import com.tonic.jaloc.impl.arrays.PCharWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveStack;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PCharStack extends AbstractPrimitiveStack<PCharArray, PCharWriter> {
    public PCharStack() {
        this(0);
    }

    public PCharStack(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PCharStack(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PCharArray(allocator, initialCapacity));
    }

    @Override
    protected PCharArray createArray(NativeAllocator allocator, long capacity) {
        return new PCharArray(allocator, capacity);
    }

    public void push(char value) {
        PCharWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
    }

    public void pushAll(char... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        PCharWriter writer = appendWriter(values.length);

        for (char value : values) {
            writer.put(value);
        }

        commitWriter();
    }

    public char pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        long lastIndex = size() - 1;
        char value = elementsUnchecked().getUnchecked(lastIndex);
        decrementSize();
        return value;
    }

    public char peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        return elementsUnchecked().getUnchecked(size() - 1);
    }
}
