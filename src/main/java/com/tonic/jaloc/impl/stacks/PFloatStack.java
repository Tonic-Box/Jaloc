package com.tonic.jaloc.impl.stacks;

import com.tonic.jaloc.impl.arrays.PFloatArray;
import com.tonic.jaloc.impl.arrays.PFloatWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveStack;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PFloatStack extends AbstractPrimitiveStack<PFloatArray, PFloatWriter> {
    public PFloatStack() {
        this(0);
    }

    public PFloatStack(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PFloatStack(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PFloatArray(allocator, initialCapacity));
    }

    @Override
    protected PFloatArray createArray(NativeAllocator allocator, long capacity) {
        return new PFloatArray(allocator, capacity);
    }

    public void push(float value) {
        PFloatWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
    }

    public void pushAll(float... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        PFloatWriter writer = appendWriter(values.length);

        for (float value : values) {
            writer.put(value);
        }

        commitWriter();
    }

    public float pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        long lastIndex = size() - 1;
        float value = elements().get(lastIndex);
        decrementSize();
        return value;
    }

    public float peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        return elements().get(size() - 1);
    }
}
