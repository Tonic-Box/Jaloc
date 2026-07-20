package com.tonic.jaloc.impl.array2ds;

import com.tonic.jaloc.impl.arrays.PIntArray;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.iface.NativeAllocator;

public final class PIntArray2D implements AutoCloseable
{
    private final PIntArray array;
    private final long rows;
    private final long columns;

    public PIntArray2D(long rows, long columns)
    {
        this(SystemAllocator.getInstance(), rows, columns);
    }

    public PIntArray2D(NativeAllocator allocator, long rows, long columns)
    {
        this.rows = requireDimension(rows, "rows");
        this.columns = requireDimension(columns, "columns");
        this.array = new PIntArray(allocator, Math.multiplyExact(rows, columns));
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

    public int get(long row, long column)
    {
        return array.get(flatIndex(row, column));
    }

    public void set(long row, long column, int value)
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
