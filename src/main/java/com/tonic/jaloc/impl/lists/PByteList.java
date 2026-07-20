package com.tonic.jaloc.impl.lists;

import com.tonic.jaloc.impl.arrays.PByteArray;
import com.tonic.jaloc.impl.arrays.PByteWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveList;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PByteList extends AbstractPrimitiveList<PByteArray, PByteWriter>
{
    public PByteList() {
        this(0);
    }

    public PByteList(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PByteList(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PByteArray(allocator, initialCapacity));
    }

    @Override
    protected PByteArray createArray(NativeAllocator allocator, long capacity) {
        return new PByteArray(allocator, capacity);
    }

    public void add(byte value) {
        PByteWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
    }

    public void addAll(byte... values) {
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

    public byte get(long index)
    {
        checkElementIndex(index);
        return elementsUnchecked().getUnchecked(index);
    }

    public byte set(long index, byte value)
    {
        checkElementIndex(index);
        byte previous = elementsUnchecked().getUnchecked(index);
        elementsUnchecked().setUnchecked(index, value);
        return previous;
    }

    public byte removeLast()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        long lastIndex = size() - 1;
        byte previous = elementsUnchecked().getUnchecked(lastIndex);
        decrementSize();
        return previous;
    }

    public void sort()
    {
        elements().sort(0, size());
    }

    public long binarySearch(byte value)
    {
        return elements().binarySearch(0, size(), value);
    }
}
