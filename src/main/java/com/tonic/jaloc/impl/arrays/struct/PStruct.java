package com.tonic.jaloc.impl.arrays.struct;

import com.tonic.jaloc.memory.data.struct.StructField;
import com.tonic.jaloc.memory.data.struct.StructLayout;

import java.util.Objects;

/**
 * A typed view over one struct element; subclass it and expose named accessors.
 */
public class PStruct
{
    private final PStructArray<?> array;
    private long index;

    protected PStruct(PStructArray<?> array, long index)
    {
        this.array = Objects.requireNonNull(array, "array");
        this.index = index;
    }

    /**
     * @return the element index this view points at
     */
    public long index()
    {
        return index;
    }

    /**
     * Repositions this view to index.
     *
     * @param index the element index
     * @return this view
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException if closed
     */
    public PStruct moveTo(long index)
    {
        array.checkStructIndex(index);
        this.index = index;
        return this;
    }

    /**
     * @return the struct layout
     */
    public StructLayout layout()
    {
        return array.layout();
    }

    protected final PStructArray<?> array()
    {
        return array;
    }

    /**
     * Reads the BOOLEAN field.
     *
     * @param field the field handle
     * @return the value
     * @throws IllegalArgumentException if field is foreign or not BOOLEAN
     * @throws IllegalStateException if closed
     */
    public boolean getBoolean(StructField field)
    {
        return array.getBoolean(index, field);
    }

    /**
     * Writes the BOOLEAN field.
     *
     * @param field the field handle
     * @param value the value to store
     * @return this view
     * @throws IllegalArgumentException if field is foreign or not BOOLEAN
     * @throws IllegalStateException if closed
     */
    public PStruct setBoolean(StructField field, boolean value)
    {
        array.setBoolean(index, field, value);
        return this;
    }

    /**
     * Reads the BOOLEAN field by name.
     *
     * @param name the field name
     * @return the value
     * @throws IllegalArgumentException if unknown or not BOOLEAN
     * @throws IllegalStateException if closed
     */
    public boolean getBoolean(String name)
    {
        return getBoolean(array.layout().field(name));
    }

    /**
     * Writes the BOOLEAN field by name.
     *
     * @param name the field name
     * @param value the value to store
     * @return this view
     * @throws IllegalArgumentException if unknown or not BOOLEAN
     * @throws IllegalStateException if closed
     */
    public PStruct setBoolean(String name, boolean value)
    {
        return setBoolean(array.layout().field(name), value);
    }

    /**
     * Reads the BYTE field.
     *
     * @param field the field handle
     * @return the value
     * @throws IllegalArgumentException if field is foreign or not BYTE
     * @throws IllegalStateException if closed
     */
    public byte getByte(StructField field)
    {
        return array.getByte(index, field);
    }

    /**
     * Writes the BYTE field.
     *
     * @param field the field handle
     * @param value the value to store
     * @return this view
     * @throws IllegalArgumentException if field is foreign or not BYTE
     * @throws IllegalStateException if closed
     */
    public PStruct setByte(StructField field, byte value)
    {
        array.setByte(index, field, value);
        return this;
    }

    /**
     * Reads the BYTE field by name.
     *
     * @param name the field name
     * @return the value
     * @throws IllegalArgumentException if unknown or not BYTE
     * @throws IllegalStateException if closed
     */
    public byte getByte(String name)
    {
        return getByte(array.layout().field(name));
    }

    /**
     * Writes the BYTE field by name.
     *
     * @param name the field name
     * @param value the value to store
     * @return this view
     * @throws IllegalArgumentException if unknown or not BYTE
     * @throws IllegalStateException if closed
     */
    public PStruct setByte(String name, byte value)
    {
        return setByte(array.layout().field(name), value);
    }

    /**
     * Reads the SHORT field.
     *
     * @param field the field handle
     * @return the value
     * @throws IllegalArgumentException if field is foreign or not SHORT
     * @throws IllegalStateException if closed
     */
    public short getShort(StructField field)
    {
        return array.getShort(index, field);
    }

    /**
     * Writes the SHORT field.
     *
     * @param field the field handle
     * @param value the value to store
     * @return this view
     * @throws IllegalArgumentException if field is foreign or not SHORT
     * @throws IllegalStateException if closed
     */
    public PStruct setShort(StructField field, short value)
    {
        array.setShort(index, field, value);
        return this;
    }

    /**
     * Reads the SHORT field by name.
     *
     * @param name the field name
     * @return the value
     * @throws IllegalArgumentException if unknown or not SHORT
     * @throws IllegalStateException if closed
     */
    public short getShort(String name)
    {
        return getShort(array.layout().field(name));
    }

    /**
     * Writes the SHORT field by name.
     *
     * @param name the field name
     * @param value the value to store
     * @return this view
     * @throws IllegalArgumentException if unknown or not SHORT
     * @throws IllegalStateException if closed
     */
    public PStruct setShort(String name, short value)
    {
        return setShort(array.layout().field(name), value);
    }

    /**
     * Reads the CHAR field.
     *
     * @param field the field handle
     * @return the value
     * @throws IllegalArgumentException if field is foreign or not CHAR
     * @throws IllegalStateException if closed
     */
    public char getChar(StructField field)
    {
        return array.getChar(index, field);
    }

    /**
     * Writes the CHAR field.
     *
     * @param field the field handle
     * @param value the value to store
     * @return this view
     * @throws IllegalArgumentException if field is foreign or not CHAR
     * @throws IllegalStateException if closed
     */
    public PStruct setChar(StructField field, char value)
    {
        array.setChar(index, field, value);
        return this;
    }

    /**
     * Reads the CHAR field by name.
     *
     * @param name the field name
     * @return the value
     * @throws IllegalArgumentException if unknown or not CHAR
     * @throws IllegalStateException if closed
     */
    public char getChar(String name)
    {
        return getChar(array.layout().field(name));
    }

    /**
     * Writes the CHAR field by name.
     *
     * @param name the field name
     * @param value the value to store
     * @return this view
     * @throws IllegalArgumentException if unknown or not CHAR
     * @throws IllegalStateException if closed
     */
    public PStruct setChar(String name, char value)
    {
        return setChar(array.layout().field(name), value);
    }

    /**
     * Reads the INT field.
     *
     * @param field the field handle
     * @return the value
     * @throws IllegalArgumentException if field is foreign or not INT
     * @throws IllegalStateException if closed
     */
    public int getInt(StructField field)
    {
        return array.getInt(index, field);
    }

    /**
     * Writes the INT field.
     *
     * @param field the field handle
     * @param value the value to store
     * @return this view
     * @throws IllegalArgumentException if field is foreign or not INT
     * @throws IllegalStateException if closed
     */
    public PStruct setInt(StructField field, int value)
    {
        array.setInt(index, field, value);
        return this;
    }

    /**
     * Reads the INT field by name.
     *
     * @param name the field name
     * @return the value
     * @throws IllegalArgumentException if unknown or not INT
     * @throws IllegalStateException if closed
     */
    public int getInt(String name)
    {
        return getInt(array.layout().field(name));
    }

    /**
     * Writes the INT field by name.
     *
     * @param name the field name
     * @param value the value to store
     * @return this view
     * @throws IllegalArgumentException if unknown or not INT
     * @throws IllegalStateException if closed
     */
    public PStruct setInt(String name, int value)
    {
        return setInt(array.layout().field(name), value);
    }

    /**
     * Reads the LONG field.
     *
     * @param field the field handle
     * @return the value
     * @throws IllegalArgumentException if field is foreign or not LONG
     * @throws IllegalStateException if closed
     */
    public long getLong(StructField field)
    {
        return array.getLong(index, field);
    }

    /**
     * Writes the LONG field.
     *
     * @param field the field handle
     * @param value the value to store
     * @return this view
     * @throws IllegalArgumentException if field is foreign or not LONG
     * @throws IllegalStateException if closed
     */
    public PStruct setLong(StructField field, long value)
    {
        array.setLong(index, field, value);
        return this;
    }

    /**
     * Reads the LONG field by name.
     *
     * @param name the field name
     * @return the value
     * @throws IllegalArgumentException if unknown or not LONG
     * @throws IllegalStateException if closed
     */
    public long getLong(String name)
    {
        return getLong(array.layout().field(name));
    }

    /**
     * Writes the LONG field by name.
     *
     * @param name the field name
     * @param value the value to store
     * @return this view
     * @throws IllegalArgumentException if unknown or not LONG
     * @throws IllegalStateException if closed
     */
    public PStruct setLong(String name, long value)
    {
        return setLong(array.layout().field(name), value);
    }

    /**
     * Reads the FLOAT field.
     *
     * @param field the field handle
     * @return the value
     * @throws IllegalArgumentException if field is foreign or not FLOAT
     * @throws IllegalStateException if closed
     */
    public float getFloat(StructField field)
    {
        return array.getFloat(index, field);
    }

    /**
     * Writes the FLOAT field.
     *
     * @param field the field handle
     * @param value the value to store
     * @return this view
     * @throws IllegalArgumentException if field is foreign or not FLOAT
     * @throws IllegalStateException if closed
     */
    public PStruct setFloat(StructField field, float value)
    {
        array.setFloat(index, field, value);
        return this;
    }

    /**
     * Reads the FLOAT field by name.
     *
     * @param name the field name
     * @return the value
     * @throws IllegalArgumentException if unknown or not FLOAT
     * @throws IllegalStateException if closed
     */
    public float getFloat(String name)
    {
        return getFloat(array.layout().field(name));
    }

    /**
     * Writes the FLOAT field by name.
     *
     * @param name the field name
     * @param value the value to store
     * @return this view
     * @throws IllegalArgumentException if unknown or not FLOAT
     * @throws IllegalStateException if closed
     */
    public PStruct setFloat(String name, float value)
    {
        return setFloat(array.layout().field(name), value);
    }

    /**
     * Reads the DOUBLE field.
     *
     * @param field the field handle
     * @return the value
     * @throws IllegalArgumentException if field is foreign or not DOUBLE
     * @throws IllegalStateException if closed
     */
    public double getDouble(StructField field)
    {
        return array.getDouble(index, field);
    }

    /**
     * Writes the DOUBLE field.
     *
     * @param field the field handle
     * @param value the value to store
     * @return this view
     * @throws IllegalArgumentException if field is foreign or not DOUBLE
     * @throws IllegalStateException if closed
     */
    public PStruct setDouble(StructField field, double value)
    {
        array.setDouble(index, field, value);
        return this;
    }

    /**
     * Reads the DOUBLE field by name.
     *
     * @param name the field name
     * @return the value
     * @throws IllegalArgumentException if unknown or not DOUBLE
     * @throws IllegalStateException if closed
     */
    public double getDouble(String name)
    {
        return getDouble(array.layout().field(name));
    }

    /**
     * Writes the DOUBLE field by name.
     *
     * @param name the field name
     * @param value the value to store
     * @return this view
     * @throws IllegalArgumentException if unknown or not DOUBLE
     * @throws IllegalStateException if closed
     */
    public PStruct setDouble(String name, double value)
    {
        return setDouble(array.layout().field(name), value);
    }

    /**
     * Zeroes every field.
     *
     * @throws IllegalStateException if closed
     */
    public void clear()
    {
        array.clearStruct(index);
    }
}
