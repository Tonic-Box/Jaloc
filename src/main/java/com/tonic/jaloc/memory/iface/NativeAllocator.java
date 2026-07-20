package com.tonic.jaloc.memory.iface;

import com.tonic.jaloc.memory.internal.MemoryBlock;

public interface NativeAllocator
{
    MemoryBlock allocate(long bytes, int alignment);

    default MemoryBlock allocate(long bytes) {
        return allocate(bytes, 8);
    }

    default MemoryBlock allocateZeroed(long bytes, int alignment) {
        MemoryBlock block = allocate(bytes, alignment);
        block.region().clear();
        return block;
    }
}