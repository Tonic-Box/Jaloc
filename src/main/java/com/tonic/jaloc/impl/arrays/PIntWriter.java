package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractArrayWriter;

/**
 * A forward-only writer over a PIntArray.
 */
public final class PIntWriter extends AbstractArrayWriter
{
    private final PIntArray array;

    PIntWriter(PIntArray array)
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
    public PIntWriter put(int value)
    {
        array.setUnchecked(nextIndex(), value);
        return this;
    }
}
