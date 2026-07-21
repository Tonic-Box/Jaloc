package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.memory.iface.NativeAllocator;

/**
 * The contiguous-storage engine behind lists, stacks, and heaps.
 */
public abstract class AbstractNativeList<A extends AbstractNativeArray<W>, W extends AbstractArrayWriter> extends AbstractNativeCollection<A, W>
{
    private W writer;

    protected AbstractNativeList(NativeAllocator allocator, A initialArray)
    {
        super(allocator, initialArray);

        this.writer = initialArray.writer();
    }

    /**
     * Zeroes the live range and empties the collection.
     *
     * @throws IllegalStateException if closed
     */
    public final void clear()
    {
        ensureOpen();

        if (size() != 0)
        {
            elements().clearRange(0, size());
        }

        size(0);
        writer.position(0);
    }

    /**
     * Grows capacity to at least requiredCapacity.
     *
     * @param requiredCapacity the minimum capacity
     * @throws IllegalArgumentException if requiredCapacity is negative
     * @throws IllegalStateException if closed
     */
    public final void ensureCapacity(long requiredCapacity)
    {
        ensureOpen();

        if (requiredCapacity < 0)
        {
            throw new IllegalArgumentException("requiredCapacity cannot be negative");
        }

        if (requiredCapacity <= capacity())
        {
            return;
        }

        replaceArray(growCapacity(capacity(), requiredCapacity));
        writer = elements().writer(size());
    }

    /**
     * Shrinks capacity to the current size.
     *
     * @throws IllegalStateException if closed
     */
    public final void trimToSize()
    {
        ensureOpen();

        if (size() == capacity())
        {
            return;
        }

        replaceArray(size());
        writer = elements().writer(size());
    }

    protected final W appendWriter(long additionalElements)
    {
        ensureOpen();

        if (additionalElements < 0)
        {
            throw new IllegalArgumentException("additionalElements cannot be negative");
        }

        long requiredCapacity = Math.addExact(sizeUnchecked(), additionalElements);

        ensureCapacity(requiredCapacity);

        writer.position(sizeUnchecked());
        return writer;
    }

    protected final void commitWriter()
    {
        ensureOpen();

        long position = writer.position();

        if (position < sizeUnchecked() || position > capacity())
        {
            throw new IllegalStateException("Invalid writer position: " + position);
        }

        size(position);
    }

    protected final void decrementSize()
    {
        ensureOpen();

        if (sizeUnchecked() == 0)
        {
            throw new IllegalStateException("List is empty");
        }

        size(sizeUnchecked() - 1);
        writer.position(sizeUnchecked());
    }

    @Override
    protected void migrateElements(A source, A destination)
    {
        if (size() != 0)
        {
            source.memory().copyTo(0, destination.memory(), 0, source.byteSize(size()));
        }
    }
}
