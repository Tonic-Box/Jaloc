package com.tonic.jaloc.impl.lists;

import com.tonic.jaloc.impl.arrays.PIntArray;
import com.tonic.jaloc.impl.arrays.PIntWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveList;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PIntList extends AbstractPrimitiveList<PIntArray, PIntWriter> {
    public PIntList() {
        this(0);
    }

    public PIntList(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PIntList(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PIntArray(allocator, initialCapacity));
    }

    @Override
    protected PIntArray createArray(NativeAllocator allocator, long capacity) {
        return new PIntArray(allocator, capacity);
    }

    public void add(int value) {
        PIntWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
    }

    public void addAll(int... values) {
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

    public int get(long index)
    {
        checkElementIndex(index);
        return elements().get(index);
    }

    public int set(long index, int value)
    {
        checkElementIndex(index);
        int previous = elements().get(index);
        elements().set(index, value);
        return previous;
    }

    public int removeLast()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        long lastIndex = size() - 1;
        int previous = elements().get(lastIndex);
        decrementSize();
        return previous;
    }

    public void sort()
    {
        elements().sort(0, size());
    }

    public long binarySearch(int value)
    {
        return elements().binarySearch(0, size(), value);
    }
}
