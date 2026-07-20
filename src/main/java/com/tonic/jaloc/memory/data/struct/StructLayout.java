package com.tonic.jaloc.memory.data.struct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StructLayout
{
    private final Object identity;
    private final Map<String, StructField> fieldsByName;
    private final List<StructField> fields;
    private final long size;
    private final long stride;
    private final int alignment;

    private StructLayout(Object identity, Map<String, StructField> fields, long size, long stride, int alignment)
    {
        this.identity = identity;
        this.fieldsByName = Collections.unmodifiableMap(new LinkedHashMap<>(fields));
        this.fields = Collections.unmodifiableList(new ArrayList<>(fields.values()));
        this.size = size;
        this.stride = stride;
        this.alignment = alignment;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public StructField field(String name)
    {
        StructField field = fieldsByName.get(name);
        if (field == null)
        {
            throw new IllegalArgumentException("Unknown struct field: " + name);
        }
        return field;
    }

    public List<StructField> fields()
    {
        return fields;
    }

    public long size()
    {
        return size;
    }

    public long stride()
    {
        return stride;
    }

    public int alignment()
    {
        return alignment;
    }

    public void validateField(StructField field, StructType expectedType)
    {
        if (field == null)
        {
            throw new NullPointerException("field");
        }
        if (!field.belongsTo(identity))
        {
            throw new IllegalArgumentException("Field does not belong to this layout");
        }
        if (field.getType() != expectedType)
        {
            throw new IllegalArgumentException("Field '" + field.getName() + "' is " + field.getType() + ", not " + expectedType);
        }
    }

    public static final class Builder
    {
        private final Object identity = new Object();
        private final Map<String, StructField> fields = new LinkedHashMap<>();
        private long offset;
        private int alignment = 1;
        private boolean built;

        private Builder()
        {
        }

        public Builder boolField(String name)
        {
            return add(name, StructType.BOOLEAN);
        }

        public Builder byteField(String name)
        {
            return add(name, StructType.BYTE);
        }

        public Builder shortField(String name)
        {
            return add(name, StructType.SHORT);
        }

        public Builder charField(String name)
        {
            return add(name, StructType.CHAR);
        }

        public Builder intField(String name)
        {
            return add(name, StructType.INT);
        }

        public Builder longField(String name)
        {
            return add(name, StructType.LONG);
        }

        public Builder floatField(String name)
        {
            return add(name, StructType.FLOAT);
        }

        public Builder doubleField(String name)
        {
            return add(name, StructType.DOUBLE);
        }

        public StructLayout build()
        {
            ensureNotBuilt();
            if (fields.isEmpty())
            {
                throw new IllegalStateException("Struct must contain at least one field");
            }
            built = true;
            long stride = alignUp(offset, alignment);
            return new StructLayout(identity, fields, offset, stride, alignment);
        }

        private Builder add(String name, StructType type)
        {
            ensureNotBuilt();
            if (name == null || name.trim().isEmpty())
            {
                throw new IllegalArgumentException("Field name cannot be empty");
            }
            if (fields.containsKey(name))
            {
                throw new IllegalArgumentException("Duplicate struct field: " + name);
            }
            offset = alignUp(offset, type.getAlignment());
            StructField field = new StructField(identity, name, type, offset);
            fields.put(name, field);
            offset = Math.addExact(offset, type.getSize());
            alignment = Math.max(alignment, type.getAlignment());
            return this;
        }

        private void ensureNotBuilt()
        {
            if (built)
            {
                throw new IllegalStateException("Builder has already been built");
            }
        }

        private static long alignUp(long value, int alignment)
        {
            long mask = alignment - 1L;
            return Math.addExact(value, mask) & ~mask;
        }
    }
}