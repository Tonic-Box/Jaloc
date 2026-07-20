package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;

public final class PByteArray extends AbstractPrimitiveArray<PByteWriter>
{
    public PByteArray(long length) {
        super(ElementSize.BYTE, length);
    }

    public PByteArray(NativeAllocator allocator, long length) {
        super(allocator, ElementSize.BYTE, length);
    }

    public byte get(long index)
    {
        return readByte(index);
    }

    public void set(long index, byte value)
    {
        writeByte(index, value);
    }

    @Override
    public PByteWriter writer()
    {
        return new PByteWriter(this);
    }
}
