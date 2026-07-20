package com.tonic.jaloc.impl.stacks;

import com.tonic.jaloc.impl.arrays.PShortArray;
import com.tonic.jaloc.impl.arrays.PShortWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveStack;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PShortStack extends AbstractPrimitiveStack<PShortArray, PShortWriter> {
    public PShortStack() {
        this(0);
    }

    public PShortStack(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PShortStack(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PShortArray(allocator, initialCapacity));
    }

    @Override
    protected PShortArray createArray(NativeAllocator allocator, long capacity) {
        return new PShortArray(allocator, capacity);
    }

    public void push(short value) {
        PShortWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
    }

    public void pushAll(short... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        PShortWriter writer = appendWriter(values.length);

        for (short value : values) {
            writer.put(value);
        }

        commitWriter();
    }

    public short pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        long lastIndex = size() - 1;
        short value = elements().get(lastIndex);
        decrementSize();
        return value;
    }

    public short peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        return elements().get(size() - 1);
    }
}
