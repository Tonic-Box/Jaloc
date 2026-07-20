package com.tonic.jaloc.impl.lists;

import com.tonic.jaloc.impl.arrays.PShortArray;
import com.tonic.jaloc.impl.arrays.PShortWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveList;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PShortList extends AbstractPrimitiveList<PShortArray, PShortWriter>
{
    public PShortList() {
        this(0);
    }

    public PShortList(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PShortList(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PShortArray(allocator, initialCapacity));
    }

    @Override
    protected PShortArray createArray(NativeAllocator allocator, long capacity) {
        return new PShortArray(allocator, capacity);
    }

    public void add(short value) {
        PShortWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
    }

    public void addAll(short... values) {
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

    public short get(long index)
    {
        checkElementIndex(index);
        return elements().get(index);
    }

    public short set(long index, short value)
    {
        checkElementIndex(index);
        short previous = elements().get(index);
        elements().set(index, value);
        return previous;
    }

    public short removeLast()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        long lastIndex = size() - 1;
        checkElementIndex(lastIndex);
        short previous = elements().get(lastIndex);
        decrementSize();
        return previous;
    }

    public void sort()
    {
        elements().sort(0, size());
    }

    public long binarySearch(short value)
    {
        return elements().binarySearch(0, size(), value);
    }
}
