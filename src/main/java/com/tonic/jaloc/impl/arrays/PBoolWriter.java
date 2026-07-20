package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractArrayWriter;

public final class PBoolWriter extends AbstractArrayWriter
{
    private final PBoolArray array;

    PBoolWriter(PBoolArray array)
    {
        super(array.length());
        this.array = array;
    }

    public PBoolWriter put(boolean value)
    {
        array.setUnchecked(nextIndex(), value);
        return this;
    }
}
