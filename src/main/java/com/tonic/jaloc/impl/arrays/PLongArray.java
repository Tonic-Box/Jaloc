package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;

public final class PLongArray extends AbstractPrimitiveArray<PLongWriter>
{
    public PLongArray(long length)
    {
        super(ElementSize.QWORD, length);
    }

    public PLongArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.QWORD, length);
    }

    public long get(long index)
    {
        return readLong(index);
    }

    public void set(long index, long value)
    {
        writeLong(index, value);
    }

    @Override
    public PLongWriter writer()
    {
        return new PLongWriter(this);
    }
}
