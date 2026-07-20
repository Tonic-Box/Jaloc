package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;

public final class PDoubleArray extends AbstractPrimitiveArray<PDoubleWriter>
{
    public PDoubleArray(long length)
    {
        super(ElementSize.QWORD, length);
    }

    public PDoubleArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.QWORD, length);
    }

    public double get(long index)
    {
        return readDouble(index);
    }

    public void set(long index, double value)
    {
        writeDouble(index, value);
    }

    @Override
    public PDoubleWriter writer()
    {
        return new PDoubleWriter(this);
    }
}
