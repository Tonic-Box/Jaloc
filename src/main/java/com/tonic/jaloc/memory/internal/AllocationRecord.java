package com.tonic.jaloc.memory.internal;

import com.tonic.jaloc.memory.iface.AllocationOwner;

public final class AllocationRecord
{
    private final AllocationOwner owner;
    private final long rawAddress;
    private final long size;
    private final long reservedBytes;
    private final int alignment;

    private boolean closed;
    private NativeCleaner.Registration registration;

    public AllocationRecord(AllocationOwner owner, long rawAddress, long size, long reservedBytes, int alignment) {
        this.owner = owner;
        this.rawAddress = rawAddress;
        this.size = size;
        this.reservedBytes = reservedBytes;
        this.alignment = alignment;
    }

    public long rawAddress() {
        return rawAddress;
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

    synchronized void registration(NativeCleaner.Registration registration) {
        this.registration = registration;
    }

    synchronized boolean close() {
        if (closed) {
            return false;
        }

        closed = true;
        owner.release(this);

        if (registration != null) {
            NativeCleaner.unregister(registration);
        }

        return true;
    }
}
