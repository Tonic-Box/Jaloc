package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.memory.iface.NativeAllocator;

/**
 * The marker base for primitive heaps.
 */
public abstract class AbstractPrimitiveHeap<A extends AbstractPrimitiveArray<W>, W extends AbstractArrayWriter> extends AbstractNativeList<A, W>
{
    protected AbstractPrimitiveHeap(NativeAllocator allocator, A initialArray)
    {
        super(allocator, initialArray);
    }
}
