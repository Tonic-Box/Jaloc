package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;

public final class PShortArray extends AbstractPrimitiveArray<PShortWriter>
{
    public PShortArray(long length)
    {
        super(ElementSize.WORD, length);
    }

    public PShortArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.WORD, length);
    }

    public short get(long index)
    {
        return readShort(index);
    }

    public void set(long index, short value)
    {
        writeShort(index, value);
    }

    @Override
    public PShortWriter writer()
    {
        return new PShortWriter(this);
    }
}
