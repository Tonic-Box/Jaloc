package com.tonic.jaloc.impl.lists;

import com.tonic.jaloc.impl.arrays.PFloatArray;
import com.tonic.jaloc.impl.arrays.PFloatWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveList;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PFloatList extends AbstractPrimitiveList<PFloatArray, PFloatWriter>
{
    public PFloatList() {
        this(0);
    }

    public PFloatList(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PFloatList(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PFloatArray(allocator, initialCapacity));
    }

    @Override
    protected PFloatArray createArray(NativeAllocator allocator, long capacity) {
        return new PFloatArray(allocator, capacity);
    }

    @Override
    protected void copyElements(PFloatArray source, PFloatArray destination, long elementCount) {
        for (long i = 0; i < elementCount; i++) {
            destination.set(i, source.get(i));
        }
    }

    public void add(float value) {
        PFloatWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
    }

    public void addAll(float... values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        if (values.length == 0) {
            return;
        }

        PFloatWriter writer = appendWriter(values.length);

        for (float value : values) {
            writer.put(value);
        }

        commitWriter();
    }

    public float get(long index)
    {
        checkElementIndex(index);
        return elements().get(index);
    }

    public float set(long index, float value)
    {
        checkElementIndex(index);
        float previous = elements().get(index);
        elements().set(index, value);
        return previous;
    }

    public float removeLast()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        long lastIndex = size() - 1;
        checkElementIndex(lastIndex);
        float previous = elements().get(lastIndex);
        decrementSize();
        return previous;
    }
}
