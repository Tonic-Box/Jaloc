package com.tonic.jaloc.impl.arrays.struct;

@FunctionalInterface
public interface StructViewFactory<T extends PStruct>
{
    T create(PStructArray<T> array, long index);
}