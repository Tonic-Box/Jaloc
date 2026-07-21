package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.memory.iface.NativeAllocator;

/**
 * The marker base for primitive ring buffers.
 */
public abstract class AbstractPrimitiveRingBuffer<A extends AbstractPrimitiveArray<W>, W extends AbstractArrayWriter> extends AbstractNativeRing<A, W>
{
    protected AbstractPrimitiveRingBuffer(NativeAllocator allocator, A initialArray)
    {
        super(allocator, initialArray);
    }
}
