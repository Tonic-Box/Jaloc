package com.tonic.jaloc.impl.arrays.struct;

import com.tonic.jaloc.memory.abs.AbstractArrayWriter;

import java.util.Objects;

public final class PStructWriter<T extends PStruct> extends AbstractArrayWriter
{
    private final PStructArray<T> array;

    public PStructWriter(PStructArray<T> array)
    {
        super(Objects.requireNonNull(array, "array").length());
        this.array = array;
    }

    public T next()
    {
        return array.at(nextIndex());
    }
}