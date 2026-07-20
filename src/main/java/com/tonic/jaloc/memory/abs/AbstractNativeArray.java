package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.MemoryBlock;
import com.tonic.jaloc.memory.internal.MemoryRegion;

import java.util.Objects;

public abstract class AbstractNativeArray<W extends AbstractArrayWriter> implements AutoCloseable
{
    private final long length;
    private final long byteSize;

    private final MemoryBlock block;
    private final MemoryRegion memory;
    private final long baseAddress;

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
            allocatedMemory.clear();
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

    public final long length()
    {
        return length;
    }

    public final long byteSize()
    {
        return byteSize;
    }

    public final boolean isOpen()
    {
        return block.isOpen();
    }

    public final boolean isEmpty()
    {
        return length == 0;
    }

    public final void clear()
    {
        memory.clear();
    }

    public abstract void clearRange(long fromIndex, long toIndex);

    public abstract W writer();

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
        if (!block.isOpen())
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

    @Override
    public final void close()
    {
        block.close();
    }
}