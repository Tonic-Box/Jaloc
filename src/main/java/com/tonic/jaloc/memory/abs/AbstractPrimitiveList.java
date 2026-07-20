package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.memory.iface.NativeAllocator;

public abstract class AbstractPrimitiveList<A extends AbstractPrimitiveArray<W>, W extends AbstractArrayWriter> extends AbstractNativeList<A, W>
{
    protected AbstractPrimitiveList(NativeAllocator allocator, A initialArray)
    {
        super(allocator, initialArray);
    }
}