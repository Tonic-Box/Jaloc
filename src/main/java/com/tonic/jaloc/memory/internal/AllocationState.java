package com.tonic.jaloc.memory.internal;

import com.tonic.jaloc.memory.iface.AllocationOwner;

public final class AllocationState
{
    private final AllocationOwner owner;
    private final long rawAddress;
    private final long address;
    private final long size;
    private final long reservedBytes;
    private final int alignment;

    private volatile boolean closed;

    public AllocationState(AllocationOwner owner, long rawAddress, long address, long size, long reservedBytes, int alignment) {
        this.owner = owner;
        this.rawAddress = rawAddress;
        this.address = address;
        this.size = size;
        this.reservedBytes = reservedBytes;
        this.alignment = alignment;
    }

    public long rawAddress() {
        return rawAddress;
    }

    long address() {
        ensureOpen();
        return address;
    }

    long size() {
        return size;
    }

    long reservedBytes() {
        return reservedBytes;
    }

    int alignment() {
        return alignment;
    }

    boolean isOpen() {
        return !closed && owner.isOpen();
    }

    void ensureOpen() {
        if (!isOpen()) {
            throw new IllegalStateException("Native memory has been released");
        }
    }

    synchronized void close() {
        if (closed) {
            return;
        }

        closed = true;
        owner.release(this);
    }
}