package com.tonic.jaloc.memory.internal;

public final class MemoryBlock implements AutoCloseable
{
    private final AllocationState state;
    private final MemoryRegion region;

    public MemoryBlock(AllocationState state) {
        this.state = state;
        this.region = new MemoryRegion(state, state.address(), state.size());
    }

    public MemoryRegion region() {
        state.ensureOpen();
        return region;
    }

    public long size() {
        return state.size();
    }

    public boolean isOpen() {
        return state.isOpen();
    }

    @Override
    public void close() {
        state.close();
    }
}