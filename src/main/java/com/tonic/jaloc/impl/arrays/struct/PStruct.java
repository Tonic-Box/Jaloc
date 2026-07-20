package com.tonic.jaloc.impl.arrays.struct;

import com.tonic.jaloc.memory.data.struct.StructField;
import com.tonic.jaloc.memory.data.struct.StructLayout;

import java.util.Objects;

public class PStruct
{
    private final PStructArray<?> array;
    private final long index;

    protected PStruct(PStructArray<?> array, long index)
    {
        this.array = Objects.requireNonNull(array, "array");
        this.index = index;
    }

    public long index()
    {
        return index;
    }

    public StructLayout layout()
    {
        return array.layout();
    }

    protected final PStructArray<?> array()
    {
        return array;
    }

    public boolean getBoolean(StructField field)
    {
        return array.getBoolean(index, field);
    }

    public PStruct setBoolean(StructField field, boolean value)
    {
        array.setBoolean(index, field, value);
        return this;
    }

    public boolean getBoolean(String name)
    {
        return getBoolean(array.layout().field(name));
    }

    public PStruct setBoolean(String name, boolean value)
    {
        return setBoolean(array.layout().field(name), value);
    }

    public byte getByte(StructField field)
    {
        return array.getByte(index, field);
    }

    public PStruct setByte(StructField field, byte value)
    {
        array.setByte(index, field, value);
        return this;
    }

    public byte getByte(String name)
    {
        return getByte(array.layout().field(name));
    }

    public PStruct setByte(String name, byte value)
    {
        return setByte(array.layout().field(name), value);
    }

    public short getShort(StructField field)
    {
        return array.getShort(index, field);
    }

    public PStruct setShort(StructField field, short value)
    {
        array.setShort(index, field, value);
        return this;
    }

    public short getShort(String name)
    {
        return getShort(array.layout().field(name));
    }

    public PStruct setShort(String name, short value)
    {
        return setShort(array.layout().field(name), value);
    }

    public char getChar(StructField field)
    {
        return array.getChar(index, field);
    }

    public PStruct setChar(StructField field, char value)
    {
        array.setChar(index, field, value);
        return this;
    }

    public char getChar(String name)
    {
        return getChar(array.layout().field(name));
    }

    public PStruct setChar(String name, char value)
    {
        return setChar(array.layout().field(name), value);
    }

    public int getInt(StructField field)
    {
        return array.getInt(index, field);
    }

    public PStruct setInt(StructField field, int value)
    {
        array.setInt(index, field, value);
        return this;
    }

    public int getInt(String name)
    {
        return getInt(array.layout().field(name));
    }

    public PStruct setInt(String name, int value)
    {
        return setInt(array.layout().field(name), value);
    }

    public long getLong(StructField field)
    {
        return array.getLong(index, field);
    }

    public PStruct setLong(StructField field, long value)
    {
        array.setLong(index, field, value);
        return this;
    }

    public long getLong(String name)
    {
        return getLong(array.layout().field(name));
    }

    public PStruct setLong(String name, long value)
    {
        return setLong(array.layout().field(name), value);
    }

    public float getFloat(StructField field)
    {
        return array.getFloat(index, field);
    }

    public PStruct setFloat(StructField field, float value)
    {
        array.setFloat(index, field, value);
        return this;
    }

    public float getFloat(String name)
    {
        return getFloat(array.layout().field(name));
    }

    public PStruct setFloat(String name, float value)
    {
        return setFloat(array.layout().field(name), value);
    }

    public double getDouble(StructField field)
    {
        return array.getDouble(index, field);
    }

    public PStruct setDouble(StructField field, double value)
    {
        array.setDouble(index, field, value);
        return this;
    }

    public double getDouble(String name)
    {
        return getDouble(array.layout().field(name));
    }

    public PStruct setDouble(String name, double value)
    {
        return setDouble(array.layout().field(name), value);
    }

    public void clear()
    {
        array.clearStruct(index);
    }
}