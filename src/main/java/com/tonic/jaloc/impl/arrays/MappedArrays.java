package com.tonic.jaloc.impl.arrays;

final class MappedArrays
{
    private MappedArrays()
    {
    }

    static long requireLength(long length)
    {
        if (length <= 0)
        {
            throw new IllegalArgumentException("length must be positive");
        }

        return length;
    }

    static long elementCount(long fileBytes, int elementSize)
    {
        if (fileBytes % elementSize != 0)
        {
            throw new IllegalArgumentException("file length " + fileBytes + " is not a multiple of the element width " + elementSize);
        }

        return fileBytes / elementSize;
    }
}
