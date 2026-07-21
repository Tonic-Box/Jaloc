package com.tonic.jaloc.impl.arrays.struct;

import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.Objects;

/**
 * A fixed-shape 2D struct array over a single native allocation, addressed row-major.
 */
public final class PStructArray2D<T extends PStruct> implements AutoCloseable
{
    private final PStructArray<T> array;
    private final long rows;
    private final long columns;

    /**
     * Allocates a rows x columns grid on the system allocator, zeroed.
     *
     * @param viewFactory creates the typed views
     * @param layout the struct layout
     * @param rows the row count
     * @param columns the column count
     * @throws IllegalArgumentException if either dimension is negative
     */
    public PStructArray2D(StructViewFactory<T> viewFactory, StructLayout layout, long rows, long columns)
    {
        this(SystemAllocator.getInstance(), viewFactory, layout, rows, columns);
    }

    /**
     * Allocates a rows x columns grid on the given allocator, zeroed.
     *
     * @param allocator the allocator to source memory from
     * @param viewFactory creates the typed views
     * @param layout the struct layout
     * @param rows the row count
     * @param columns the column count
     * @throws IllegalArgumentException if either dimension is negative
     */
    public PStructArray2D(NativeAllocator allocator, StructViewFactory<T> viewFactory, StructLayout layout, long rows, long columns)
    {
        Objects.requireNonNull(allocator, "allocator");

        this.rows = requireDimension(rows, "rows");
        this.columns = requireDimension(columns, "columns");
        this.array = new PStructArray<>(allocator, viewFactory, layout, Math.multiplyExact(rows, columns));
    }

    /**
     * @return the struct layout
     */
    public StructLayout layout()
    {
        return array.layout();
    }

    /**
     * @return the row count
     */
    public long rows()
    {
        return rows;
    }

    /**
     * @return the column count
     */
    public long columns()
    {
        return columns;
    }

    /**
     * @return total cell count, rows x columns
     */
    public long length()
    {
        return array.length();
    }

    /**
     * @return true until closed
     */
    public boolean isOpen()
    {
        return array.isOpen();
    }

    /**
     * Creates a view of the cell at row, column.
     *
     * @param row the row index
     * @param column the column index
     * @return a fresh view
     * @throws IndexOutOfBoundsException if either index is out of range
     * @throws IllegalStateException if closed
     */
    public T at(long row, long column)
    {
        return array.at(flatIndex(row, column));
    }

    /**
     * Zeroes the cell at row, column.
     *
     * @param row the row index
     * @param column the column index
     * @throws IndexOutOfBoundsException if either index is out of range
     * @throws IllegalStateException if closed
     */
    public void clearStruct(long row, long column)
    {
        array.clearStruct(flatIndex(row, column));
    }

    /**
     * Zeroes every cell.
     *
     * @throws IllegalStateException if closed
     */
    public void clear()
    {
        array.clear();
    }

    /**
     * Releases the backing native memory. Safe to call more than once.
     */
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
