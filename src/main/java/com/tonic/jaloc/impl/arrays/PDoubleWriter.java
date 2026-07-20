package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractArrayWriter;

public final class PDoubleWriter extends AbstractArrayWriter
{
    private final PDoubleArray array;

    PDoubleWriter(PDoubleArray array)
    {
        super(array.length());
        this.array = array;
    }

    public PDoubleWriter put(double value)
    {
        array.setUnchecked(nextIndex(), value);
        return this;
    }
}
