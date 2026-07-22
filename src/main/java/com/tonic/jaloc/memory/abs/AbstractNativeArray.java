package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.MemoryBlock;
import com.tonic.jaloc.memory.internal.MemoryRegion;

import java.util.Objects;

/**
 * The base of all native arrays: one aligned allocation, cached base address, and lifecycle.
 */
public abstract class AbstractNativeArray<W extends AbstractArrayWriter> implements AutoCloseable
{
    private final long length;
    private final long byteSize;

    private final MemoryBlock block;
    private final MemoryRegion memory;
    private final long baseAddress;

    private boolean closed;

    protected AbstractNativeArray(long length, long byteSize, int alignment)
    {
        this(SystemAllocator.getInstance(), length, byteSize, alignment);
    }

    protected AbstractNativeArray(NativeAllocator allocator, long length, long byteSize, int alignment)
    {
        Objects.requireNonNull(allocator, "allocator");

        if (length < 0)
        {
            throw new IllegalArgumentException("length cannot be negative");
        }

        if (byteSize < 0)
        {
            throw new IllegalArgumentException("byteSize cannot be negative");
        }

        this.length = length;
        this.byteSize = byteSize;

        MemoryBlock allocatedBlock = allocator.allocate(byteSize, alignment);

        MemoryRegion allocatedMemory;
        long allocatedAddress;

        try
        {
            allocatedMemory = allocatedBlock.region();

            if (allocator.clearRequired())
            {
                allocatedMemory.clear();
            }

            allocatedAddress = allocatedMemory.address();
        }
        catch (RuntimeException | Error e)
        {
            allocatedBlock.close();
            throw e;
        }

        this.block = allocatedBlock;
        this.memory = allocatedMemory;
        this.baseAddress = allocatedAddress;
    }

    /**
     * @return the element count
     */
    public final long length()
    {
        return length;
    }

    /**
     * @return the allocation payload size in bytes
     */
    public final long byteSize()
    {
        return byteSize;
    }

    /**
     * @return true until closed
     */
    public final boolean isOpen()
    {
        return block.isOpen();
    }

    /**
     * @return true if length is zero
     */
    public final boolean isEmpty()
    {
        return length == 0;
    }

    /**
     * Zeroes every element.
     *
     * @throws IllegalStateException if closed
     */
    public final void clear()
    {
        memory.clear();
    }

    /**
     * Zeroes fromIndex inclusive to toIndex exclusive.
     *
     * @param fromIndex the range start, inclusive
     * @param toIndex the range end, exclusive
     * @throws IndexOutOfBoundsException if the range is out of bounds
     * @throws IllegalStateException if closed
     */
    public abstract void clearRange(long fromIndex, long toIndex);

    protected abstract long byteSize(long elementCount);

    /**
     * @return a fresh writer at position zero
     */
    public abstract W writer();

    /**
     * Creates a writer at position.
     *
     * @param position the starting position
     * @return the writer
     * @throws IndexOutOfBoundsException if position is out of range
     */
    public final W writer(long position)
    {
        W writer = writer();
        writer.position(position);
        return writer;
    }

    protected final MemoryRegion memory()
    {
        return memory;
    }

    protected final long baseAddress()
    {
        return baseAddress;
    }

    protected final void ensureOpen()
    {
        if (closed)
        {
            throw new IllegalStateException("Native memory has been released");
        }
    }

    protected final void checkIndex(long index)
    {
        ensureOpen();

        if (index < 0 || index >= length)
        {
            throw new IndexOutOfBoundsException("index=" + index + ", length=" + length);
        }
    }

    protected final void checkRange(long fromIndex, long toIndex)
    {
        ensureOpen();

        if (fromIndex < 0 || toIndex < fromIndex || toIndex > length)
        {
            throw new IndexOutOfBoundsException("fromIndex=" + fromIndex + ", toIndex=" + toIndex + ", length=" + length);
        }
    }

    /**
     * Releases the backing native memory. Safe to call more than once.
     */
    @Override
    public final void close()
    {
        closed = true;
        block.close();
    }
}
