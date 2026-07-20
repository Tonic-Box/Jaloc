package com.tonic.jaloc.impl.lists;

import com.tonic.jaloc.impl.arrays.PLongArray;
import com.tonic.jaloc.impl.arrays.PLongWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveList;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PLongList extends AbstractPrimitiveList<PLongArray, PLongWriter>
{
    public PLongList() {
        this(0);
    }

    public PLongList(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PLongList(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PLongArray(allocator, initialCapacity));
    }

    @Override
    protected PLongArray createArray(NativeAllocator allocator, long capacity) {
        return new PLongArray(allocator, capacity);
    }

    public void add(long value) {
        PLongWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
    }

    public void addAll(long... values) {
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

    public long get(long index)
    {
        checkElementIndex(index);
        return elements().get(index);
    }

    public long set(long index, long value)
    {
        checkElementIndex(index);
        long previous = elements().get(index);
        elements().set(index, value);
        return previous;
    }

    public long removeLast()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        long lastIndex = size() - 1;
        checkElementIndex(lastIndex);
        long previous = elements().get(lastIndex);
        decrementSize();
        return previous;
    }

    public void sort()
    {
        elements().sort(0, size());
    }

    public long binarySearch(long value)
    {
        return elements().binarySearch(0, size(), value);
    }
}
