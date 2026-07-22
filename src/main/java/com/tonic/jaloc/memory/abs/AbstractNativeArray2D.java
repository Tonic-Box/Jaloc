package com.tonic.jaloc.memory.abs;

/**
 * The fixed-shape scaffolding behind the row-major 2D grids: dimensions, flat addressing, and lifecycle.
 */
public abstract class AbstractNativeArray2D implements AutoCloseable
{
    private final long rows;
    private final long columns;

    protected AbstractNativeArray2D(long rows, long columns)
    {
        this.rows = requireDimension(rows, "rows");
        this.columns = requireDimension(columns, "columns");
    }

    protected abstract AbstractNativeArray<?> backing();

    /**
     * @return the row count
     */
    public final long rows()
    {
        return rows;
    }

    /**
     * @return the column count
     */
    public final long columns()
    {
        return columns;
    }

    /**
     * @return total cell count, rows x columns
     */
    public final long length()
    {
        return backing().length();
    }

    /**
     * @return true until closed
     */
    public final boolean isOpen()
    {
        return backing().isOpen();
    }

    /**
     * Clears every cell.
     *
     * @throws IllegalStateException if closed
     */
    public final void clear()
    {
        backing().clear();
    }

    /**
     * Releases the backing native memory. Safe to call more than once.
     */
    @Override
    public final void close()
    {
        backing().close();
    }

    protected final long flatIndex(long row, long column)
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
