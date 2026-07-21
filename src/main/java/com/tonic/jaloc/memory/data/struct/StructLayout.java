package com.tonic.jaloc.memory.data.struct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable field layout with computed offsets, stride, and alignment.
 */
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

    /**
     * @return a fresh builder
     */
    public static Builder builder()
    {
        return new Builder();
    }

    /**
     * Looks up a field by name.
     *
     * @param name the field name
     * @return the field
     * @throws IllegalArgumentException if unknown
     */
    public StructField field(String name)
    {
        StructField field = fieldsByName.get(name);
        if (field == null)
        {
            throw new IllegalArgumentException("Unknown struct field: " + name);
        }
        return field;
    }

    /**
     * @return the fields in declaration order
     */
    public List<StructField> fields()
    {
        return fields;
    }

    /**
     * @return the unpadded field span in bytes
     */
    public long size()
    {
        return size;
    }

    /**
     * @return the aligned per-element stride in bytes
     */
    public long stride()
    {
        return stride;
    }

    /**
     * @return the layout alignment
     */
    public int alignment()
    {
        return alignment;
    }

    /**
     * Rejects foreign fields and type mismatches.
     *
     * @param field the field to validate
     * @param expectedType the required type
     * @throws NullPointerException if field is null
     * @throws IllegalArgumentException if field is foreign or the wrong type
     */
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

    /**
     * Accumulates fields in declaration order; single use.
     */
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

        /**
         * Adds a BOOL field.
         *
         * @param name the field name
         * @return this builder
         * @throws IllegalArgumentException if name is empty or duplicate
         * @throws IllegalStateException if already built
         */
        public Builder boolField(String name)
        {
            return add(name, StructType.BOOLEAN);
        }

        /**
         * Adds a BYTE field.
         *
         * @param name the field name
         * @return this builder
         * @throws IllegalArgumentException if name is empty or duplicate
         * @throws IllegalStateException if already built
         */
        public Builder byteField(String name)
        {
            return add(name, StructType.BYTE);
        }

        /**
         * Adds a SHORT field.
         *
         * @param name the field name
         * @return this builder
         * @throws IllegalArgumentException if name is empty or duplicate
         * @throws IllegalStateException if already built
         */
        public Builder shortField(String name)
        {
            return add(name, StructType.SHORT);
        }

        /**
         * Adds a CHAR field.
         *
         * @param name the field name
         * @return this builder
         * @throws IllegalArgumentException if name is empty or duplicate
         * @throws IllegalStateException if already built
         */
        public Builder charField(String name)
        {
            return add(name, StructType.CHAR);
        }

        /**
         * Adds a INT field.
         *
         * @param name the field name
         * @return this builder
         * @throws IllegalArgumentException if name is empty or duplicate
         * @throws IllegalStateException if already built
         */
        public Builder intField(String name)
        {
            return add(name, StructType.INT);
        }

        /**
         * Adds a LONG field.
         *
         * @param name the field name
         * @return this builder
         * @throws IllegalArgumentException if name is empty or duplicate
         * @throws IllegalStateException if already built
         */
        public Builder longField(String name)
        {
            return add(name, StructType.LONG);
        }

        /**
         * Adds a FLOAT field.
         *
         * @param name the field name
         * @return this builder
         * @throws IllegalArgumentException if name is empty or duplicate
         * @throws IllegalStateException if already built
         */
        public Builder floatField(String name)
        {
            return add(name, StructType.FLOAT);
        }

        /**
         * Adds a DOUBLE field.
         *
         * @param name the field name
         * @return this builder
         * @throws IllegalArgumentException if name is empty or duplicate
         * @throws IllegalStateException if already built
         */
        public Builder doubleField(String name)
        {
            return add(name, StructType.DOUBLE);
        }

        /**
         * Builds the layout.
         *
         * @return the layout
         * @throws IllegalStateException if already built or no fields were added
         */
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
