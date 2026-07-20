package com.tonic.jaloc.impl.arrays.struct;

import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.Objects;

public final class PStructArray2D<T extends PStruct> implements AutoCloseable
{
    private final PStructArray<T> array;
    private final long rows;
    private final long columns;

    public PStructArray2D(StructViewFactory<T> viewFactory, StructLayout layout, long rows, long columns)
    {
        this(SystemAllocator.getInstance(), viewFactory, layout, rows, columns);
    }

    public PStructArray2D(NativeAllocator allocator, StructViewFactory<T> viewFactory, StructLayout layout, long rows, long columns)
    {
        Objects.requireNonNull(allocator, "allocator");

        this.rows = requireDimension(rows, "rows");
        this.columns = requireDimension(columns, "columns");
        this.array = new PStructArray<>(allocator, viewFactory, layout, Math.multiplyExact(rows, columns));
    }

    public StructLayout layout()
    {
        return array.layout();
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

    public T at(long row, long column)
    {
        return array.at(flatIndex(row, column));
    }

    public void clearStruct(long row, long column)
    {
        array.clearStruct(flatIndex(row, column));
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
