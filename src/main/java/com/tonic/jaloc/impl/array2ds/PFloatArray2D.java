package com.tonic.jaloc.impl.array2ds;

import com.tonic.jaloc.impl.arrays.PFloatArray;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.iface.NativeAllocator;

public final class PFloatArray2D implements AutoCloseable
{
    private final PFloatArray array;
    private final long rows;
    private final long columns;

    public PFloatArray2D(long rows, long columns)
    {
        this(SystemAllocator.getInstance(), rows, columns);
    }

    public PFloatArray2D(NativeAllocator allocator, long rows, long columns)
    {
        this.rows = requireDimension(rows, "rows");
        this.columns = requireDimension(columns, "columns");
        this.array = new PFloatArray(allocator, Math.multiplyExact(rows, columns));
    }

    public long rows()
    {
        return rows;
    }

    public long columns()
    {
        return columns;
    }

    public long length()
    {
        return array.length();
    }

    public boolean isOpen()
    {
        return array.isOpen();
    }

    public float get(long row, long column)
    {
        return array.get(flatIndex(row, column));
    }

    public void set(long row, long column, float value)
    {
        array.set(flatIndex(row, column), value);
    }

    public void clear()
    {
        array.clear();
    }

    @Override
    public void close()
    {
        array.close();
    }

    private long flatIndex(long row, long column)
    {
        if (row < 0 || row >= rows || column < 0 || column >= columns)
        {
            throw new IndexOutOfBoundsException("row=" + row + ", column=" + column + ", rows=" + rows + ", columns=" + columns);
        }

        return row * columns + column;
    }

    private static long requireDimension(long value, String name)
    {
        if (value < 0)
        {
            throw new IllegalArgumentException(name + " cannot be negative");
        }

        return value;
    }
}
