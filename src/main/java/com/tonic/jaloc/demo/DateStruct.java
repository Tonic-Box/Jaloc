package com.tonic.jaloc.demo;

import com.tonic.jaloc.impl.arrays.struct.PStruct;
import com.tonic.jaloc.impl.arrays.struct.PStructArray;
import com.tonic.jaloc.memory.data.struct.StructField;
import com.tonic.jaloc.memory.data.struct.StructLayout;

public final class DateStruct extends PStruct
{
    public static final StructLayout LAYOUT =
            StructLayout.builder()
                    .byteField("month")
                    .byteField("day")
                    .shortField("year")
                    .build();

    private static final StructField MONTH = LAYOUT.field("month");

    private static final StructField DAY = LAYOUT.field("day");

    private static final StructField YEAR = LAYOUT.field("year");

    public DateStruct(PStructArray<DateStruct> array, long index)
    {
        super(array, index);
    }

    public int month()
    {
        return Byte.toUnsignedInt(getByte(MONTH));
    }

    public DateStruct month(int month)
    {
        setByte(MONTH, (byte) month);
        return this;
    }

    public int day()
    {
        return Byte.toUnsignedInt(getByte(DAY));
    }

    public DateStruct day(int day)
    {
        setByte(DAY, (byte) day);
        return this;
    }

    public int year()
    {
        return Short.toUnsignedInt(getShort(YEAR));
    }

    public DateStruct year(int year)
    {
        setShort(YEAR, (short) year);
        return this;
    }

    @Override
    public String toString()
    {
        return month() + "/" + day() + "/" + year();
    }
}