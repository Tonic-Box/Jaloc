package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractArrayWriter;

/**
 * A forward-only writer over a PLongArray.
 */
public final class PLongWriter extends AbstractArrayWriter
{
    private final PLongArray array;

    PLongWriter(PLongArray array)
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
    public PLongWriter put(long value)
    {
        array.setUnchecked(nextIndex(), value);
        return this;
    }
}
