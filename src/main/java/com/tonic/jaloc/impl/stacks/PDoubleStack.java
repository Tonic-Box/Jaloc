package com.tonic.jaloc.impl.stacks;

import com.tonic.jaloc.impl.arrays.PDoubleArray;
import com.tonic.jaloc.impl.arrays.PDoubleWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveStack;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PDoubleStack extends AbstractPrimitiveStack<PDoubleArray, PDoubleWriter> {
    public PDoubleStack() {
        this(0);
    }

    public PDoubleStack(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PDoubleStack(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PDoubleArray(allocator, initialCapacity));
    }

    @Override
    protected PDoubleArray createArray(NativeAllocator allocator, long capacity) {
        return new PDoubleArray(allocator, capacity);
    }

    public void push(double value) {
        PDoubleWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
    }

    public void pushAll(double... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        PDoubleWriter writer = appendWriter(values.length);

        for (double value : values) {
            writer.put(value);
        }

        commitWriter();
    }

    public double pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        long lastIndex = size() - 1;
        double value = elements().get(lastIndex);
        decrementSize();
        return value;
    }

    public double peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        return elements().get(size() - 1);
    }
}
