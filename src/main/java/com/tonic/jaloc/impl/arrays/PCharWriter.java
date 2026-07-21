package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractArrayWriter;

/**
 * A forward-only writer over a PCharArray.
 */
public final class PCharWriter extends AbstractArrayWriter
{
    private final PCharArray array;

    PCharWriter(PCharArray array)
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
    public PCharWriter put(char value)
    {
        array.setUnchecked(nextIndex(), value);
        return this;
    }
}
