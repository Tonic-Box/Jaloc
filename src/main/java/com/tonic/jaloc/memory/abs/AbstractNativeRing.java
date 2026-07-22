package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.memory.iface.NativeAllocator;

/**
 * The ring engine behind queues, deques, and buffers: wraparound head and tail over one array.
 */
public abstract class AbstractNativeRing<A extends AbstractNativeArray<W>, W extends AbstractArrayWriter> extends AbstractNativeCollection<A, W>
{
    private long head;

    protected AbstractNativeRing(NativeAllocator allocator, A initialArray)
    {
        super(allocator, initialArray);
    }

    protected final long head()
    {
        return head;
    }

    protected final long physicalIndex(long logicalIndex)
    {
        long physical = head + logicalIndex;
        long currentCapacity = capacity();

        if (physical >= currentCapacity)
        {
            physical -= currentCapacity;
        }

        return physical;
    }

    protected final long reserveTail()
    {
        ensureOpen();
        ensureRingCapacity(Math.addExact(size(), 1));
        return physicalIndex(size());
    }

    protected final void commitTail()
    {
        size(size() + 1);
    }

    protected final long reserveHead()
    {
        ensureOpen();
        ensureRingCapacity(Math.addExact(size(), 1));

        head = (head == 0 ? capacity() : head) - 1;

        return head;
    }

    protected final void commitHead()
    {
        size(size() + 1);
    }

    protected final long tailIndex()
    {
        checkElementIndex(0);
        return physicalIndex(size() - 1);
    }

    protected final void shrinkTail()
    {
        size(size() - 1);

        if (size() == 0)
        {
            head = 0;
        }
    }

    protected final long headIndex()
    {
        checkElementIndex(0);
        return physicalIndex(0);
    }

    protected final void advanceHead()
    {
        rotateHead();
        size(size() - 1);

        if (size() == 0)
        {
            head = 0;
        }
    }

    protected final void rotateHead()
    {
        long next = head + 1;

        if (next >= capacity())
        {
            next = 0;
        }

        head = next;
    }

    protected final void ensureRingCapacity(long requiredCapacity)
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
        head = 0;
    }

    protected static long requireCapacity(long capacity)
    {
        if (capacity <= 0)
        {
            throw new IllegalArgumentException("capacity must be positive");
        }

        return capacity;
    }

    protected final void trimRing()
    {
        ensureOpen();

        if (size() == capacity())
        {
            return;
        }

        replaceArray(size());
        head = 0;
    }

    @Override
    protected void migrateElements(A source, A destination)
    {
        long size = size();

        if (size == 0)
        {
            return;
        }

        long firstSegment = Math.min(size, source.length() - head);

        source.memory().copyTo(source.byteSize(head), destination.memory(), 0, source.byteSize(firstSegment));

        long secondSegment = size - firstSegment;

        if (secondSegment != 0)
        {
            source.memory().copyTo(0, destination.memory(), source.byteSize(firstSegment), source.byteSize(secondSegment));
        }
    }

    /**
     * Empties the ring and zeroes its storage.
     *
     * @throws IllegalStateException if closed
     */
    public final void clear()
    {
        ensureOpen();

        elements().clear();
        head = 0;
        size(0);
    }
}
