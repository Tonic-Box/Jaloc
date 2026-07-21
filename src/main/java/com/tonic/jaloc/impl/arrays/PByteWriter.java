package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractArrayWriter;

/**
 * A forward-only writer over a PByteArray.
 */
public final class PByteWriter extends AbstractArrayWriter
{
    private final PByteArray array;
    PByteWriter(PByteArray array) {
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
    public PByteWriter put(byte value)
    {
        array.setUnchecked(nextIndex(), value);
        return this;
    }
}
