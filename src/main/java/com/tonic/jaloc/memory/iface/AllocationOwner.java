package com.tonic.jaloc.memory.iface;

import com.tonic.jaloc.memory.internal.AllocationRecord;

/**
 * Owns allocations and handles their release.
 */
public interface AllocationOwner
{
    /**
     * Releases the allocation's memory.
     *
     * @param allocation the record to free
     */
    void release(AllocationRecord allocation);

    /**
     * @return true while this owner can still back its allocations
     */
    boolean isOpen();
}
