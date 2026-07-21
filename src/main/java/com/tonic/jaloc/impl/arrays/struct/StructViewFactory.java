package com.tonic.jaloc.impl.arrays.struct;

/**
 * Creates typed views over struct array elements.
 */
@FunctionalInterface
public interface StructViewFactory<T extends PStruct>
{
    /**
     * @param array the backing array
     * @param index the element index
     * @return the view
     */
    T create(PStructArray<T> array, long index);
}
