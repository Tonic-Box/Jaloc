package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.memory.iface.NativeAllocator;

/**
 * The marker base for primitive deques.
 */
public abstract class AbstractPrimitiveDeque<A extends AbstractPrimitiveArray<W>, W extends AbstractArrayWriter> extends AbstractNativeRing<A, W>
{
    protected AbstractPrimitiveDeque(NativeAllocator allocator, A initialArray)
    {
        super(allocator, initialArray);
    }
}
