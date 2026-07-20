package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractArrayWriter;

public final class PIntWriter extends AbstractArrayWriter
{
    private final PIntArray array;

    PIntWriter(PIntArray array)
    {
        super(array.length());
        this.array = array;
    }

    public PIntWriter put(int value)
    {
        array.set(nextIndex(), value);
        return this;
    }
}
