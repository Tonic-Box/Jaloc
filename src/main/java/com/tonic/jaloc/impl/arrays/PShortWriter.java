package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractArrayWriter;

public final class PShortWriter extends AbstractArrayWriter
{
    private final PShortArray array;
    PShortWriter(PShortArray array)
    {
        super(array.length());
        this.array = array;
    }

    public PShortWriter put(short value)
    {
        array.setUnchecked(nextIndex(), value);
        return this;
    }
}
