package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;

public final class PIntArray extends AbstractPrimitiveArray<PIntWriter>
{
    public PIntArray(long length)
    {
        super(ElementSize.DWORD, length);
    }

    public PIntArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.DWORD, length);
    }

    public int get(long index)
    {
        return readInt(index);
    }

    public void set(long index, int value)
    {
        writeInt(index, value);
    }

    @Override
    public PIntWriter writer()
    {
        return new PIntWriter(this);
    }
}