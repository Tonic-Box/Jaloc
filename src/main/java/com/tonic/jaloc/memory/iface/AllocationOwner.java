package com.tonic.jaloc.memory.iface;

import com.tonic.jaloc.memory.internal.AllocationRecord;

public interface AllocationOwner
{
    void release(AllocationRecord allocation);

    boolean isOpen();
}
