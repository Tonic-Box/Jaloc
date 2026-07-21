package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.memory.iface.NativeAllocator;

/**
 * The marker base for primitive stacks.
 */
public abstract class AbstractPrimitiveStack<A extends AbstractPrimitiveArray<W>, W extends AbstractArrayWriter> extends AbstractNativeList<A, W>
{
    protected AbstractPrimitiveStack(NativeAllocator allocator, A initialArray)
    {
        super(allocator, initialArray);
    }
}
