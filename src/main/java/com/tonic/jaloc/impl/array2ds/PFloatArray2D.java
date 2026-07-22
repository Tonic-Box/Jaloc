package com.tonic.jaloc.impl.array2ds;

import com.tonic.jaloc.impl.arrays.PFloatArray;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractNativeArray;
import com.tonic.jaloc.memory.abs.AbstractNativeArray2D;
import com.tonic.jaloc.memory.iface.NativeAllocator;

/**
 * A fixed-shape 2D float array over a single native allocation, addressed row-major.
 */
public final class PFloatArray2D extends AbstractNativeArray2D
{
    private final PFloatArray array;

    /**
     * Allocates a rows x columns grid on the system allocator, zeroed.
     *
     * @param rows the row count
     * @param columns the column count
     * @throws IllegalArgumentException if either dimension is negative
     */
    public PFloatArray2D(long rows, long columns)
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
    public PFloatArray2D(NativeAllocator allocator, long rows, long columns)
    {
        super(rows, columns);

        this.array = new PFloatArray(allocator, Math.multiplyExact(rows, columns));
    }

    @Override
    protected AbstractNativeArray<?> backing()
    {
        return array;
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
    public float get(long row, long column)
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
    public void set(long row, long column, float value)
    {
        array.set(flatIndex(row, column), value);
    }
}
