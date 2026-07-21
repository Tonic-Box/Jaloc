package com.tonic.jaloc.memory.data.struct;

/**
 * One field's name, type, and offset within a layout.
 */
public final class StructField {

    private final Object layoutIdentity;
    private final String name;
    private final StructType type;
    private final long offset;

    StructField(Object layoutIdentity, String name, StructType type, long offset) {
        this.layoutIdentity = layoutIdentity;
        this.name = name;
        this.type = type;
        this.offset = offset;
    }

    /**
     * @return the field name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the field type
     */
    public StructType getType() {
        return type;
    }

    /**
     * @return the byte offset within the struct
     */
    public long getOffset() {
        return offset;
    }

    boolean belongsTo(Object identity) {
        return layoutIdentity == identity;
    }

    @Override
    public String toString() {
        return type + " " + name + " @" + offset;
    }
}
