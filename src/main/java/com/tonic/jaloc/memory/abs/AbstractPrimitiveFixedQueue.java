package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.memory.iface.NativeAllocator;

public abstract class AbstractPrimitiveFixedQueue<A extends AbstractPrimitiveArray<W>, W extends AbstractArrayWriter> extends AbstractNativeRing<A, W>
{
    protected AbstractPrimitiveFixedQueue(NativeAllocator allocator, A initialArray)
    {
        super(allocator, initialArray);
    }
}
