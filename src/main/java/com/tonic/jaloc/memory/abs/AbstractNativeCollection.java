package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.Objects;

/**
 * The base of all native collections: allocator, backing array, size, and lifecycle.
 */
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

    /**
     * @return the element count
     * @throws IllegalStateException if closed
     */
    public final long size()
    {
        ensureOpen();
        return size;
    }

    /**
     * @return true if no elements are present
     * @throws IllegalStateException if closed
     */
    public final boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * @return the backing array length
     * @throws IllegalStateException if closed
     */
    public final long capacity()
    {
        ensureOpen();
        return array.length();
    }

    /**
     * @return true until closed
     */
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

    protected final A elementsUnchecked()
    {
        return array;
    }

    protected final long sizeUnchecked()
    {
        return size;
    }

    protected final long elementsBaseAddress()
    {
        return array.baseAddress();
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
        if (!open)
        {
            throw new IllegalStateException("Native collection has been closed");
        }
    }

    /**
     * Releases the backing native memory. Safe to call more than once.
     */
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
