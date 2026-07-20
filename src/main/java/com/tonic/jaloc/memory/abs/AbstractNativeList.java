package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.Objects;

public abstract class AbstractNativeList<A extends AbstractNativeArray<W>, W extends AbstractArrayWriter> implements AutoCloseable
{
    private static final long DEFAULT_CAPACITY = 8;

    private final NativeAllocator allocator;

    private A array;
    private W writer;
    private long size;
    private boolean open = true;

    protected AbstractNativeList(NativeAllocator allocator, A initialArray)
    {
        this.allocator = Objects.requireNonNull(allocator, "allocator");

        this.array = Objects.requireNonNull(initialArray, "initialArray");

        this.writer = initialArray.writer();
    }

    protected abstract A createArray(NativeAllocator allocator, long capacity);

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

    public final void clear()
    {
        ensureOpen();

        if (size != 0)
        {
            array.clearRange(0, size);
        }

        size = 0;
        writer.position(0);
    }

    public final void ensureCapacity(long requiredCapacity)
    {
        ensureOpen();

        if (requiredCapacity < 0)
        {
            throw new IllegalArgumentException("requiredCapacity cannot be negative");
        }

        if (requiredCapacity <= array.length())
        {
            return;
        }

        replaceArray(growCapacity(array.length(), requiredCapacity));
    }

    public final void trimToSize()
    {
        ensureOpen();

        if (size == array.length())
        {
            return;
        }

        replaceArray(size);
    }

    protected final W appendWriter(long additionalElements)
    {
        ensureOpen();

        if (additionalElements < 0)
        {
            throw new IllegalArgumentException("additionalElements cannot be negative");
        }

        long requiredCapacity = Math.addExact(size, additionalElements);

        ensureCapacity(requiredCapacity);

        writer.position(size);
        return writer;
    }

    protected final void commitWriter()
    {
        ensureOpen();

        long position = writer.position();

        if (position < size || position > array.length())
        {
            throw new IllegalStateException("Invalid writer position: " + position);
        }

        size = position;
    }

    protected final A elements()
    {
        ensureOpen();
        return array;
    }

    protected final void checkElementIndex(long index)
    {
        ensureOpen();

        if (index < 0 || index >= size)
        {
            throw new IndexOutOfBoundsException("index=" + index + ", size=" + size);
        }
    }

    protected final void checkPositionIndex(long index)
    {
        ensureOpen();

        if (index < 0 || index > size)
        {
            throw new IndexOutOfBoundsException("index=" + index + ", size=" + size);
        }
    }

    protected final void decrementSize()
    {
        ensureOpen();

        if (size == 0)
        {
            throw new IllegalStateException("List is empty");
        }

        size--;
        writer.position(size);
    }

    private void replaceArray(long newCapacity)
    {
        A replacement = createArray(allocator, newCapacity);

        boolean installed = false;

        try
        {
            if (size != 0)
            {
                array.memory().copyTo(0, replacement.memory(), 0, array.byteSize(size));
            }

            W replacementWriter = replacement.writer(size);

            A previous = array;

            array = replacement;
            writer = replacementWriter;
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

    private static long growCapacity(long currentCapacity, long requiredCapacity)
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
            throw new IllegalStateException("Native list has been closed");
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