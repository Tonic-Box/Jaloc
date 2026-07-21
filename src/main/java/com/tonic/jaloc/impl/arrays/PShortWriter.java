package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractArrayWriter;

/**
 * A forward-only writer over a PShortArray.
 */
public final class PShortWriter extends AbstractArrayWriter
{
    private final PShortArray array;
    PShortWriter(PShortArray array)
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
    public PShortWriter put(short value)
    {
        array.setUnchecked(nextIndex(), value);
        return this;
    }
}
