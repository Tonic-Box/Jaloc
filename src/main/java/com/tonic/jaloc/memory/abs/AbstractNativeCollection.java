package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.Objects;

public abstract class AbstractNativeCollection<A extends AbstractNativeArray<W>, W extends AbstractArrayWriter> implements AutoCloseable
{
    private static final long DEFAULT_CAPACITY = 8;

    private final NativeAllocator allocator;

    private A array;
    private long size;
    private boolean open = true;

    protected AbstractNativeCollection(NativeAllocator allocator, A initialArray)
    {
        this.allocator = Objects.requireNonNull(allocator, "allocator");

        this.array = Objects.requireNonNull(initialArray, "initialArray");
    }

    protected abstract A createArray(NativeAllocator allocator, long capacity);

    protected abstract void migrateElements(A source, A destination);

    public final long size()
    {
        ensureOpen();
        return size;
    }

    public final boolean isEmpty()
    {
        return size() == 0;
    }

    public final long capacity()
    {
        ensureOpen();
        return array.length();
    }

    public boolean isOpen()
    {
        return open && array.isOpen();
    }

    protected final NativeAllocator allocator()
    {
        return allocator;
    }

    protected final A elements()
    {
        ensureOpen();
        return array;
    }

    protected final void size(long size)
    {
        this.size = size;
    }

    protected final void checkElementIndex(long index)
    {
        ensureOpen();

        if (index < 0 || index >= size)
        {
            throw new IndexOutOfBoundsException("index=" + index + ", size=" + size);
        }
    }

    protected final void replaceArray(long newCapacity)
    {
        A replacement = createArray(allocator, newCapacity);

        boolean installed = false;

        try
        {
            migrateElements(array, replacement);

            A previous = array;

            array = replacement;
            installed = true;

            previous.close();
        }
        finally
        {
            if (!installed)
            {
                replacement.close();
            }
        }
    }

    protected static long growCapacity(long currentCapacity, long requiredCapacity)
    {
        if (currentCapacity == 0)
        {
            return Math.max(DEFAULT_CAPACITY, requiredCapacity);
        }

        long growth = Math.max(1, currentCapacity >>> 1);

        long candidate;

        try
        {
            candidate = Math.addExact(currentCapacity, growth);
        }
        catch (ArithmeticException ignored)
        {
            candidate = Long.MAX_VALUE;
        }

        return Math.max(candidate, requiredCapacity);
    }

    protected final void ensureOpen()
    {
        if (!isOpen())
        {
            throw new IllegalStateException("Native collection has been closed");
        }
    }

    @Override
    public final void close()
    {
        if (!open)
        {
            return;
        }

        open = false;
        array.close();
    }
}
