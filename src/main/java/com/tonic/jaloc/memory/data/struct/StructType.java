package com.tonic.jaloc.memory.data.struct;

/**
 * Struct field types with their sizes and alignments.
 */
public enum StructType {
    BOOLEAN(1, 1),
    BYTE(1, 1),
    SHORT(2, 2),
    CHAR(2, 2),
    INT(4, 4),
    FLOAT(4, 4),
    LONG(8, 8),
    DOUBLE(8, 8)
    ;
    private final int size;
    private final int alignment;

    StructType(int size, int alignment)
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
}
