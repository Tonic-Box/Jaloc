package com.tonic.jaloc.impl.lists;

import com.tonic.jaloc.impl.arrays.PCharArray;
import com.tonic.jaloc.impl.arrays.PCharWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveList;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PCharList extends AbstractPrimitiveList<PCharArray, PCharWriter>
{
    public PCharList() {
        this(0);
    }

    public PCharList(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PCharList(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PCharArray(allocator, initialCapacity));
    }

    @Override
    protected PCharArray createArray(NativeAllocator allocator, long capacity) {
        return new PCharArray(allocator, capacity);
    }

    public void add(char value) {
        PCharWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
    }

    public void addAll(char... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        PCharWriter writer = appendWriter(values.length);

        for (char value : values) {
            writer.put(value);
        }

        commitWriter();
    }

    public char get(long index)
    {
        checkElementIndex(index);
        return elements().get(index);
    }

    public char set(long index, char value)
    {
        checkElementIndex(index);
        char previous = elements().get(index);
        elements().set(index, value);
        return previous;
    }

    public char removeLast()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }

        long lastIndex = size() - 1;
        char previous = elements().get(lastIndex);
        decrementSize();
        return previous;
    }

    public void sort()
    {
        elements().sort(0, size());
    }

    public long binarySearch(char value)
    {
        return elements().binarySearch(0, size(), value);
    }
}
