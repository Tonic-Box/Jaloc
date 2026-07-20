package com.tonic.jaloc.impl.stacks;

import com.tonic.jaloc.impl.arrays.PByteArray;
import com.tonic.jaloc.impl.arrays.PByteWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveStack;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PByteStack extends AbstractPrimitiveStack<PByteArray, PByteWriter> {
    public PByteStack() {
        this(0);
    }

    public PByteStack(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PByteStack(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PByteArray(allocator, initialCapacity));
    }

    @Override
    protected PByteArray createArray(NativeAllocator allocator, long capacity) {
        return new PByteArray(allocator, capacity);
    }

    public void push(byte value) {
        PByteWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
    }

    public void pushAll(byte... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        PByteWriter writer = appendWriter(values.length);

        for (byte value : values) {
            writer.put(value);
        }

        commitWriter();
    }

    public byte pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        long lastIndex = size() - 1;
        byte value = elements().get(lastIndex);
        decrementSize();
        return value;
    }

    public byte peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        return elements().get(size() - 1);
    }
}
