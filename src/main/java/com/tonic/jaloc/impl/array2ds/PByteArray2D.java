package com.tonic.jaloc.impl.array2ds;

import com.tonic.jaloc.impl.arrays.PByteArray;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.iface.NativeAllocator;

/**
 * A fixed-shape 2D byte array over a single native allocation, addressed row-major.
 */
public final class PByteArray2D implements AutoCloseable
{
    private final PByteArray array;
    private final long rows;
    private final long columns;

    /**
     * Allocates a rows x columns grid on the system allocator, zeroed.
     *
     * @param rows the row count
     * @param columns the column count
     * @throws IllegalArgumentException if either dimension is negative
     */
    public PByteArray2D(long rows, long columns)
    {
        this(SystemAllocator.getInstance(), rows, columns);
    }

    /**
     * Allocates a rows x columns grid on the given allocator, zeroed.
     *
     * @param allocator the allocator to source memory from
     * @param rows the row count
     * @param columns the column count
     * @throws IllegalArgumentException if either dimension is negative
     */
    public PByteArray2D(NativeAllocator allocator, long rows, long columns)
    {
        this.rows = requireDimension(rows, "rows");
        this.columns = requireDimension(columns, "columns");
        this.array = new PByteArray(allocator, Math.multiplyExact(rows, columns));
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
     * Reads the cell at row, column.
     *
     * @param row the row index
     * @param column the column index
     * @return the cell value
     * @throws IndexOutOfBoundsException if either index is out of range
     * @throws IllegalStateException if closed
     */
    public byte get(long row, long column)
    {
        return array.get(flatIndex(row, column));
    }

    /**
     * Writes the cell at row, column.
     *
     * @param row the row index
     * @param column the column index
     * @param value the value to store
     * @throws IndexOutOfBoundsException if either index is out of range
     * @throws IllegalStateException if closed
     */
    public void set(long row, long column, byte value)
    {
        array.set(flatIndex(row, column), value);
    }

    /**
     * Clears every cell to zero.
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
