package com.tonic.jaloc.memory.data.struct;

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

    public int getSize()
    {
        return size;
    }

    public int getAlignment()
    {
        return alignment;
    }
}