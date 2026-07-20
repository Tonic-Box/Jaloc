package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractArrayWriter;

public final class PCharWriter extends AbstractArrayWriter
{
    private final PCharArray array;

    PCharWriter(PCharArray array)
    {
        super(array.length());
        this.array = array;
    }

    public PCharWriter put(char value)
    {
        array.set(nextIndex(), value);
        return this;
    }
}
