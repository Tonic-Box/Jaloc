package com.tonic.jaloc.impl.lists;

import com.tonic.jaloc.impl.arrays.PDoubleArray;
import com.tonic.jaloc.impl.arrays.PDoubleWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveList;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PDoubleList extends AbstractPrimitiveList<PDoubleArray, PDoubleWriter>
{
    public PDoubleList() {
        this(0);
    }

    public PDoubleList(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PDoubleList(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PDoubleArray(allocator, initialCapacity));
    }

    @Override
    protected PDoubleArray createArray(NativeAllocator allocator, long capacity) {
        return new PDoubleArray(allocator, capacity);
    }

    public void add(double value) {
        PDoubleWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
    }

    public void addAll(double... values) {
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

    public double get(long index)
    {
        checkElementIndex(index);
        return elements().get(index);
    }

    public double set(long index, double value)
    {
        checkElementIndex(index);
        double previous = elements().get(index);
        elements().set(index, value);
        return previous;
    }

    public double removeLast()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        long lastIndex = size() - 1;
        checkElementIndex(lastIndex);
        double previous = elements().get(lastIndex);
        decrementSize();
        return previous;
    }
}
