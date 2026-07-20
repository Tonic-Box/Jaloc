package com.tonic.jaloc.impl.stacks;

import com.tonic.jaloc.impl.arrays.PBoolArray;
import com.tonic.jaloc.impl.arrays.PBoolWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveStack;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;
import java.util.Objects;

public final class PBoolStack extends AbstractPrimitiveStack<PBoolArray, PBoolWriter>
{
    public PBoolStack()
    {
        this(0);
    }

    public PBoolStack(long initialCapacity)
    {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PBoolStack(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PBoolArray(allocator, initialCapacity));
    }

    @Override
    protected PBoolArray createArray(NativeAllocator allocator, long capacity) {
        return new PBoolArray(allocator, capacity);
    }

    public void push(boolean value)
    {
        PBoolWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
    }

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

    public boolean pop()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        long lastIndex = size() - 1;
        boolean value = elementsUnchecked().getUnchecked(lastIndex);
        elementsUnchecked().setUnchecked(lastIndex, false);
        decrementSize();
        return value;
    }

    public boolean peek()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        return elementsUnchecked().getUnchecked(size() - 1);
    }
}
