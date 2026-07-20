package com.tonic.jaloc.memory.data.struct;

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

    public String getName() {
        return name;
    }

    public StructType getType() {
        return type;
    }

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