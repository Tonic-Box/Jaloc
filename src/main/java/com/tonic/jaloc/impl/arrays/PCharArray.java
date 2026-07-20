package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;

public final class PCharArray extends AbstractPrimitiveArray<PCharWriter>
{
    public PCharArray(long length)
    {
        super(ElementSize.WORD, length);
    }

    public PCharArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.WORD, length);
    }

    public char get(long index)
    {
        return readChar(index);
    }

    public void set(long index, char value)
    {
        writeChar(index, value);
    }

    @Override
    public PCharWriter writer()
    {
        return new PCharWriter(this);
    }
}
