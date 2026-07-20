package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;

public final class PBoolArray extends AbstractPrimitiveArray<PBoolWriter>
{
    public PBoolArray(long length)
    {
        this(SystemAllocator.getInstance(), length);
    }

    public PBoolArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.BYTE, length, packedByteSize(length));
    }


    private static int bitMask(long index)
    {
        return 1 << (int) (index & 7L);
    }

    private static long byteOffset(long index)
    {
        return index >>> 3;
    }

    private static long packedByteSize(long length)
    {
        if(length < 0)
        {
            throw new IllegalArgumentException("length cannot be negative");
        }

        return (length >>> 3) + ((length & 7L) == 0 ? 0 : 1);
    }

    public boolean get(long index)
    {
        checkIndex(index);
        byte packed = memory().getByte(byteOffset(index));
        return (packed & bitMask(index)) != 0;
    }

    public void set(long index, boolean value)
    {
        checkIndex(index);
        long offset = byteOffset(index);
        int mask = bitMask(index);
        byte packed = memory().getByte(offset);
        packed = value ? (byte) (packed | mask) : (byte) (packed & ~mask);
        memory().putByte(offset, packed);
    }

    @Override
    public PBoolWriter writer()
    {
        return new PBoolWriter(this);
    }

    @Override
    public void clearRange(long fromIndex, long toIndex)
    {
        checkRange(fromIndex, toIndex);

        for (long i = fromIndex; i < toIndex; i++) {
            set(i, false);
        }
    }
}
