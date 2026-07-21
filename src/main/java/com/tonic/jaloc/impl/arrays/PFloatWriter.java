package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractArrayWriter;

/**
 * A forward-only writer over a PFloatArray.
 */
public final class PFloatWriter extends AbstractArrayWriter
{
    private final PFloatArray array;

    PFloatWriter(PFloatArray array)
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
    public PFloatWriter put(float value)
    {
        array.setUnchecked(nextIndex(), value);
        return this;
    }
}
