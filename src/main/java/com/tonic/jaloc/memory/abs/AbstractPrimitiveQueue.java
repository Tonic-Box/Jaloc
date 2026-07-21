package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.memory.iface.NativeAllocator;

/**
 * The marker base for primitive queues.
 */
public abstract class AbstractPrimitiveQueue<A extends AbstractPrimitiveArray<W>, W extends AbstractArrayWriter> extends AbstractNativeRing<A, W>
{
    protected AbstractPrimitiveQueue(NativeAllocator allocator, A initialArray)
    {
        super(allocator, initialArray);
    }
}
