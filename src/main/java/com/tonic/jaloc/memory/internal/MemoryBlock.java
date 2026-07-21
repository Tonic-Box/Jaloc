package com.tonic.jaloc.memory.internal;

/**
 * An owned allocation handle; closing releases the memory.
 */
public final class MemoryBlock implements AutoCloseable
{
    private final AllocationState state;
    private final MemoryRegion region;

    /**
     * Wraps an allocation state.
     *
     * @param state the allocation to own
     */
    public MemoryBlock(AllocationState state) {
        this.state = state;
        this.region = new MemoryRegion(state, state.address(), state.size());
    }

    /**
     * @return the full region over this block
     * @throws IllegalStateException if released
     */
    public MemoryRegion region() {
        state.ensureOpen();
        return region;
    }

    /**
     * @return the payload size in bytes
     */
    public long size() {
        return state.size();
    }

    /**
     * @return true until closed
     */
    public boolean isOpen() {
        return state.isOpen();
    }

    /**
     * Releases the memory. Safe to call more than once.
     */
    @Override
    public void close() {
        state.close();
    }
}
