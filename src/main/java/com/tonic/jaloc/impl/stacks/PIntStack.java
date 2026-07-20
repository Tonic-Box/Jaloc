package com.tonic.jaloc.impl.stacks;

import com.tonic.jaloc.impl.arrays.PIntArray;
import com.tonic.jaloc.impl.arrays.PIntWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveStack;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PIntStack extends AbstractPrimitiveStack<PIntArray, PIntWriter> {
    public PIntStack() {
        this(0);
    }

    public PIntStack(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PIntStack(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PIntArray(allocator, initialCapacity));
    }

    @Override
    protected PIntArray createArray(NativeAllocator allocator, long capacity) {
        return new PIntArray(allocator, capacity);
    }

    public void push(int value) {
        PIntWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
    }

    public void pushAll(int... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        PIntWriter writer = appendWriter(values.length);

        for (int value : values) {
            writer.put(value);
        }

        commitWriter();
    }

    public int pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        long lastIndex = size() - 1;
        int value = elements().get(lastIndex);
        decrementSize();
        return value;
    }

    public int peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        return elements().get(size() - 1);
    }
}
