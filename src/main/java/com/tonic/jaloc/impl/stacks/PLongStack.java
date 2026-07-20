package com.tonic.jaloc.impl.stacks;

import com.tonic.jaloc.impl.arrays.PLongArray;
import com.tonic.jaloc.impl.arrays.PLongWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveStack;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PLongStack extends AbstractPrimitiveStack<PLongArray, PLongWriter> {
    public PLongStack() {
        this(0);
    }

    public PLongStack(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PLongStack(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PLongArray(allocator, initialCapacity));
    }

    @Override
    protected PLongArray createArray(NativeAllocator allocator, long capacity) {
        return new PLongArray(allocator, capacity);
    }

    public void push(long value) {
        PLongWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
    }

    public void pushAll(long... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        PLongWriter writer = appendWriter(values.length);

        for (long value : values) {
            writer.put(value);
        }

        commitWriter();
    }

    public long pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        long lastIndex = size() - 1;
        long value = elementsUnchecked().getUnchecked(lastIndex);
        decrementSize();
        return value;
    }

    public long peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        return elementsUnchecked().getUnchecked(size() - 1);
    }
}
