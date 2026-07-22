package com.tonic.jaloc.impl.arrays.struct;

import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractNativeArray;
import com.tonic.jaloc.memory.abs.AbstractNativeArray2D;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.Objects;

/**
 * A fixed-shape 2D struct array over a single native allocation, addressed row-major.
 */
public final class PStructArray2D<T extends PStruct> extends AbstractNativeArray2D
{
    private final PStructArray<T> array;

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
        super(rows, columns);

        Objects.requireNonNull(allocator, "allocator");

        this.array = new PStructArray<>(allocator, viewFactory, layout, Math.multiplyExact(rows, columns));
    }

    @Override
    protected AbstractNativeArray<?> backing()
    {
        return array;
    }

    /**
     * @return the struct layout
     */
    public StructLayout layout()
    {
        return array.layout();
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
}
