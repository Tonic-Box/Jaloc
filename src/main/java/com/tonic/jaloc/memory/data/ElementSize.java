package com.tonic.jaloc.memory.data;

public enum ElementSize
{
    BYTE(1, 1),
    WORD(2, 2),
    DWORD(4, 4),
    QWORD(8, 8);

    private final int size;
    private final int alignment;

    ElementSize(int size, int alignment)
    {
        this.size = size;
        this.alignment = alignment;
    }

    public int getSize()
    {
        return size;
    }

    public int getAlignment()
    {
        return alignment;
    }

    public long offsetOf(long index)
    {
        return Math.multiplyExact(index, size);
    }

    public long byteSize(long length)
    {
        return Math.multiplyExact(length, size);
    }
}
