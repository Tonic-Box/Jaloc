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

    @Override
    protected void copyElements(PBoolArray source, PBoolArray destination, long elementCount) {
        for (long i = 0; i < elementCount; i++) {
            destination.set(i, source.get(i));
        }
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
        return elements().get(index);
    }

    public boolean set(long index, boolean value)
    {
        checkElementIndex(index);
        boolean previous = elements().get(index);
        elements().set(index, value);
        return previous;
    }

    public boolean removeLast()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        long lastIndex = size() - 1;
        boolean previous = elements().get(lastIndex);
        elements().set(lastIndex, false);
        decrementSize();
        return previous;
    }
}