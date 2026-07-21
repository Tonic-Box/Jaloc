package com.tonic.jaloc.impl.arrays.struct;

import com.tonic.jaloc.memory.abs.AbstractArrayWriter;

import java.util.Objects;

/**
 * A forward-only writer handing out views over a PStructArray.
 */
public final class PStructWriter<T extends PStruct> extends AbstractArrayWriter
{
    private final PStructArray<T> array;

    /**
     * Creates a writer at position zero.
     *
     * @param array the array to write into
     */
    public PStructWriter(PStructArray<T> array)
    {
        super(Objects.requireNonNull(array, "array").length());
        this.array = array;
    }

    /**
     * Returns a view of the next element and advances by one.
     *
     * @return the view
     * @throws IndexOutOfBoundsException if no capacity remains
     */
    public T next()
    {
        return array.at(nextIndex());
    }
}
