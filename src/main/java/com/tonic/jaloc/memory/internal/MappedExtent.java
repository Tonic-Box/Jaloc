package com.tonic.jaloc.memory.internal;

import java.nio.ByteBuffer;

/**
 * One live file mapping: its base address and byte length.
 */
public final class MappedExtent
{
    private final long address;
    private final long length;
    private final ByteBuffer buffer;

    MappedExtent(long address, long length, ByteBuffer buffer)
    {
        this.address = address;
        this.length = length;
        this.buffer = buffer;
    }

    /**
     * @return the mapping base address
     */
    public long address()
    {
        return address;
    }

    /**
     * @return the mapped byte length
     */
    public long length()
    {
        return length;
    }

    ByteBuffer buffer()
    {
        return buffer;
    }
}
