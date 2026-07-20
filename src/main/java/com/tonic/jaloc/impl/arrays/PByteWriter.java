package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.abs.AbstractArrayWriter;

public final class PByteWriter extends AbstractArrayWriter
{
    private final PByteArray array;
    PByteWriter(PByteArray array) {
        super(array.length());
        this.array = array;
    }

    public PByteWriter put(byte value)
    {
        array.set(nextIndex(), value);
        return this;
    }
}
