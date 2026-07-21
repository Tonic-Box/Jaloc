package com.tonic.jaloc.memory.internal;

/**
 * Tracks one allocation's aligned address and liveness.
 */
public final class AllocationState
{
    private final AllocationRecord record;
    private final long address;

    /**
     * Pairs a record with its aligned address.
     *
     * @param record the allocation record
     * @param address the aligned address
     */
    public AllocationState(AllocationRecord record, long address) {
        this.record = record;
        this.address = address;
    }

    /**
     * @return the raw allocation address
     */
    public long rawAddress() {
        return record.rawAddress();
    }

    long address() {
        ensureOpen();
        return address;
    }

    long size() {
        return record.size();
    }

    long reservedBytes() {
        return record.reservedBytes();
    }

    int alignment() {
        return record.alignment();
    }

    boolean isOpen() {
        return record.isOpen();
    }

    void ensureOpen() {
        if (!isOpen()) {
            throw new IllegalStateException("Native memory has been released");
        }
    }

    void close() {
        record.close();
    }
}
