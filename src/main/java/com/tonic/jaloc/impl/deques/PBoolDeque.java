package com.tonic.jaloc.impl.deques;

import com.tonic.jaloc.impl.arrays.PBoolArray;
import com.tonic.jaloc.impl.arrays.PBoolWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveDeque;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PBoolDeque extends AbstractPrimitiveDeque<PBoolArray, PBoolWriter>
{
    public PBoolDeque()
    {
        this(0);
    }

    public PBoolDeque(long initialCapacity)
    {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PBoolDeque(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PBoolArray(allocator, initialCapacity));
    }

    @Override
    protected PBoolArray createArray(NativeAllocator allocator, long capacity) {
        return new PBoolArray(allocator, capacity);
    }

    @Override
    protected void migrateElements(PBoolArray source, PBoolArray destination)
    {
        long size = size();

        for (long i = 0; i < size; i++)
        {
            destination.set(i, source.get(physicalIndex(i)));
        }
    }

    public void ensureCapacity(long requiredCapacity)
    {
        ensureRingCapacity(requiredCapacity);
    }

    public void trimToSize()
    {
        trimRing();
    }

    public void addFirst(boolean value)
    {
        long index = reserveHead();
        elements().set(index, value);
        commitHead();
    }

    public void addLast(boolean value)
    {
        long index = reserveTail();
        elements().set(index, value);
        commitTail();
    }

    public boolean removeFirst()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = headIndex();
        boolean value = elements().get(index);
        elements().set(index, false);
        advanceHead();
        return value;
    }

    public boolean removeLast()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        long index = tailIndex();
        boolean value = elements().get(index);
        elements().set(index, false);
        shrinkTail();
        return value;
    }

    public boolean peekFirst()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elements().get(headIndex());
    }

    public boolean peekLast()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty");
        }
        return elements().get(tailIndex());
    }
}
