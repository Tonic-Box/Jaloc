package com.tonic.jaloc.memory.data;

/**
 * Element widths with their natural alignments.
 */
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

    /**
     * @return the width in bytes
     */
    public int getSize()
    {
        return size;
    }

    /**
     * @return the natural alignment
     */
    public int getAlignment()
    {
        return alignment;
    }

    /**
     * Byte offset of index.
     *
     * @param index the element index
     * @return index times the width
     * @throws ArithmeticException on overflow
     */
    public long offsetOf(long index)
    {
        return Math.multiplyExact(index, size);
    }

    /**
     * Total bytes for length elements.
     *
     * @param length the element count
     * @return length times the width
     * @throws ArithmeticException on overflow
     */
    public long byteSize(long length)
    {
        return Math.multiplyExact(length, size);
    }
}
