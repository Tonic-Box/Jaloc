package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractArrayWriter;

/**
 * A forward-only writer over a PDoubleArray.
 */
public final class PDoubleWriter extends AbstractArrayWriter
{
    private final PDoubleArray array;

    PDoubleWriter(PDoubleArray array)
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
    public PDoubleWriter put(double value)
    {
        array.setUnchecked(nextIndex(), value);
        return this;
    }
}
