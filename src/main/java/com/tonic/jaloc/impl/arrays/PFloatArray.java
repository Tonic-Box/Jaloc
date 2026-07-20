package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;

public final class PFloatArray extends AbstractPrimitiveArray<PFloatWriter>
{
    public PFloatArray(long length)
    {
        super(ElementSize.DWORD, length);
    }

    public PFloatArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.DWORD, length);
    }

    public float get(long index)
    {
        return readFloat(index);
    }

    public void set(long index, float value)
    {
        writeFloat(index, value);
    }

    @Override
    public PFloatWriter writer()
    {
        return new PFloatWriter(this);
    }
}
