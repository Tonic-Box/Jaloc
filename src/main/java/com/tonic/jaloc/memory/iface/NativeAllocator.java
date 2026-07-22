package com.tonic.jaloc.memory.iface;

import com.tonic.jaloc.memory.internal.MemoryBlock;

/**
 * Allocates aligned native memory blocks.
 */
public interface NativeAllocator
{
    /**
     * Allocates bytes with the given alignment.
     *
     * @param bytes the payload size
     * @param alignment a positive power of two
     * @return the block
     * @throws IllegalArgumentException if bytes is negative or alignment is not a power of two
     */
    MemoryBlock allocate(long bytes, int alignment);

    /**
     * Allocates bytes with 8-byte alignment.
     *
     * @param bytes the payload size
     * @return the block
     * @throws IllegalArgumentException if bytes is negative
     */
    default MemoryBlock allocate(long bytes) {
        return allocate(bytes, 8);
    }

    /**
     * Allocates bytes with the given alignment and zeroes them.
     *
     * @param bytes the payload size
     * @param alignment a positive power of two
     * @return the block
     * @throws IllegalArgumentException if bytes is negative or alignment is not a power of two
     */
    default MemoryBlock allocateZeroed(long bytes, int alignment) {
        MemoryBlock block = allocate(bytes, alignment);

        if (clearRequired()) {
            block.region().clear();
        }

        return block;
    }

    /**
     * @return true if fresh blocks need explicit zeroing; allocators handing out pre-zeroed memory return false
     */
    default boolean clearRequired() {
        return true;
    }
}
