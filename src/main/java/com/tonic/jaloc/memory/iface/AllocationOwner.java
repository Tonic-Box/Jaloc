package com.tonic.jaloc.memory.iface;

import com.tonic.jaloc.memory.internal.AllocationState;

public interface AllocationOwner
{
    void release(AllocationState allocation);

    boolean isOpen();
}