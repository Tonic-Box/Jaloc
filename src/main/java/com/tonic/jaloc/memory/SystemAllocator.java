package com.tonic.jaloc.memory;

import com.tonic.jaloc.memory.iface.*;
import com.tonic.jaloc.memory.internal.*;

public final class SystemAllocator implements NativeAllocator, AllocationOwner
{
    private static final SystemAllocator INSTANCE = new SystemAllocator();

    private SystemAllocator() {
    }

    public static SystemAllocator getInstance() {
        return INSTANCE;
    }

    @Override
    public MemoryBlock allocate(long bytes, int alignment) {
        if (bytes < 0) {
            throw new IllegalArgumentException("bytes cannot be negative");
        }

        UnsafeMemory.validateAlignment(alignment);

        if (bytes == 0) {
            return new MemoryBlock(new AllocationState(this, 0, 0, 0, 0, alignment));
        }

        long padding = alignment - 1L;
        long reservedBytes = Math.addExact(bytes, padding);

        long rawAddress = UnsafeMemory.allocate(reservedBytes);
        boolean successful = false;

        try {
            long alignedAddress = UnsafeMemory.alignUp(rawAddress, alignment);

            AllocationState state = new AllocationState(
                    this,
                    rawAddress,
                    alignedAddress,
                    bytes,
                    reservedBytes,
                    alignment
            );

            successful = true;
            return new MemoryBlock(state);
        } finally {
            if (!successful) {
                UnsafeMemory.free(rawAddress);
            }
        }
    }

    @Override
    public void release(AllocationState allocation) {
        UnsafeMemory.free(allocation.rawAddress());
    }

    @Override
    public boolean isOpen() {
        return true;
    }
}