package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractArrayWriter;

/**
 * A forward-only writer over a PBoolArray.
 */
public final class PBoolWriter extends AbstractArrayWriter
{
    private final PBoolArray array;

    PBoolWriter(PBoolArray array)
    {
        super(array.length());
        this.array = array;
    }

    /**
     * Writes value at the current position and advances by one.
     *
     * @param value the value to write
     * @return this writer
     * @throws IndexOutOfBoundsException if no capacity remains
     */
    public PBoolWriter put(boolean value)
    {
        array.setUnchecked(nextIndex(), value);
        return this;
    }
}
