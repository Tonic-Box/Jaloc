package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractArrayWriter;

public final class PLongWriter extends AbstractArrayWriter
{
    private final PLongArray array;

    PLongWriter(PLongArray array)
    {
        super(array.length());
        this.array = array;
    }

    public PLongWriter put(long value)
    {
        array.set(nextIndex(), value);
        return this;
    }
}
