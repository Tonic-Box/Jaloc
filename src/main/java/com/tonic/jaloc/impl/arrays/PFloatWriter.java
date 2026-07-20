package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractArrayWriter;

public final class PFloatWriter extends AbstractArrayWriter
{
    private final PFloatArray array;

    PFloatWriter(PFloatArray array)
    {
        super(array.length());
        this.array = array;
    }

    public PFloatWriter put(float value)
    {
        array.set(nextIndex(), value);
        return this;
    }
}
