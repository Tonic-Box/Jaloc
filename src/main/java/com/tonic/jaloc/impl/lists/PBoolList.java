package com.tonic.jaloc.impl.lists;

import com.tonic.jaloc.impl.arrays.PBoolArray;
import com.tonic.jaloc.impl.arrays.PBoolWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveList;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;
import java.util.Objects;

public final class PBoolList extends AbstractPrimitiveList<PBoolArray, PBoolWriter>
{
    public PBoolList()
    {
        this(0);
    }

    public PBoolList(long initialCapacity)
    {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PBoolList(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PBoolArray(allocator, initialCapacity));
    }

    @Override
    protected PBoolArray createArray(NativeAllocator allocator, long capacity) {
        return new PBoolArray(allocator, capacity);
    }

    public void add(boolean value)
    {
        PBoolWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
    }

    public void addAll(boolean... values)
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

    public boolean get(long index)
    {
        checkElementIndex(index);
        return elementsUnchecked().getUnchecked(index);
    }

    public boolean set(long index, boolean value)
    {
        checkElementIndex(index);
        boolean previous = elementsUnchecked().getUnchecked(index);
        elementsUnchecked().setUnchecked(index, value);
        return previous;
    }

    public boolean removeLast()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        long lastIndex = size() - 1;
        boolean previous = elementsUnchecked().getUnchecked(lastIndex);
        elementsUnchecked().setUnchecked(lastIndex, false);
        decrementSize();
        return previous;
    }
}