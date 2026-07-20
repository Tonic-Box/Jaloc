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

    @Override
    protected void copyElements(PByteArray source, PByteArray destination, long elementCount) {
        for (long i = 0; i < elementCount; i++) {
            destination.set(i, source.get(i));
        }
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
        return elements().get(index);
    }

    public int set(long index, byte value)
    {
        checkElementIndex(index);
        int previous = elements().get(index);
        elements().set(index, value);
        return previous;
    }

    public byte removeLast()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        long lastIndex = size() - 1;
        checkElementIndex(lastIndex);
        byte previous = elements().get(lastIndex);
        decrementSize();
        return previous;
    }
}
