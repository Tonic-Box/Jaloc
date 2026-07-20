package com.tonic.jaloc.impl.sets;

import com.tonic.jaloc.impl.arrays.PLongArray;
import com.tonic.jaloc.impl.arrays.PLongWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractNativeCollection;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.Objects;
import java.util.function.LongConsumer;

public final class PLongSet extends AbstractNativeCollection<PLongArray, PLongWriter>
{
    private boolean containsZero;

    public PLongSet()
    {
        this(0);
    }

    public PLongSet(long expectedElements)
    {
        this(SystemAllocator.getInstance(), expectedElements);
    }

    public PLongSet(NativeAllocator allocator, long expectedElements)
    {
        super(allocator, new PLongArray(allocator, tableSize(expectedElements)));
    }

    @Override
    protected PLongArray createArray(NativeAllocator allocator, long capacity)
    {
        return new PLongArray(allocator, capacity);
    }

    @Override
    protected void migrateElements(PLongArray source, PLongArray destination)
    {
        long sourceLength = source.length();
        long mask = destination.length() - 1;

        for (long i = 0; i < sourceLength; i++)
        {
            long value = source.get(i);

            if (value == 0)
            {
                continue;
            }

            long position = mix(value) & mask;

            while (destination.get(position) != 0)
            {
                position = (position + 1) & mask;
            }

            destination.set(position, value);
        }
    }

    public boolean add(long value)
    {
        ensureOpen();

        if (value == 0)
        {
            if (containsZero)
            {
                return false;
            }

            containsZero = true;
            size(size() + 1);
            return true;
        }

        long mask = capacity() - 1;
        long position = mix(value) & mask;

        while (true)
        {
            long current = elements().get(position);

            if (current == 0)
            {
                break;
            }

            if (current == value)
            {
                return false;
            }

            position = (position + 1) & mask;
        }

        if (occupancy() + 1 > loadLimit())
        {
            replaceArray(capacity() << 1);

            mask = capacity() - 1;
            position = mix(value) & mask;

            while (elements().get(position) != 0)
            {
                position = (position + 1) & mask;
            }
        }

        elements().set(position, value);
        size(size() + 1);
        return true;
    }

    public boolean remove(long value)
    {
        ensureOpen();

        if (value == 0)
        {
            if (!containsZero)
            {
                return false;
            }

            containsZero = false;
            size(size() - 1);
            return true;
        }

        long mask = capacity() - 1;
        long position = mix(value) & mask;

        while (true)
        {
            long current = elements().get(position);

            if (current == 0)
            {
                return false;
            }

            if (current == value)
            {
                shiftKeys(position, mask);
                size(size() - 1);
                return true;
            }

            position = (position + 1) & mask;
        }
    }

    public boolean contains(long value)
    {
        ensureOpen();

        if (value == 0)
        {
            return containsZero;
        }

        long mask = capacity() - 1;
        long position = mix(value) & mask;

        while (true)
        {
            long current = elements().get(position);

            if (current == 0)
            {
                return false;
            }

            if (current == value)
            {
                return true;
            }

            position = (position + 1) & mask;
        }
    }

    public void forEach(LongConsumer consumer)
    {
        Objects.requireNonNull(consumer, "consumer");
        ensureOpen();

        if (containsZero)
        {
            consumer.accept(0);
        }

        long slots = capacity();

        for (long i = 0; i < slots; i++)
        {
            long current = elements().get(i);

            if (current != 0)
            {
                consumer.accept(current);
            }
        }
    }

    public void clear()
    {
        ensureOpen();

        elements().clear();
        containsZero = false;
        size(0);
    }

    private void shiftKeys(long position, long mask)
    {
        while (true)
        {
            long last = position;

            position = (position + 1) & mask;

            long current;

            while (true)
            {
                current = elements().get(position);

                if (current == 0)
                {
                    elements().set(last, 0);
                    return;
                }

                long slot = mix(current) & mask;

                if (last <= position ? (last >= slot || slot > position) : (last >= slot && slot > position))
                {
                    break;
                }

                position = (position + 1) & mask;
            }

            elements().set(last, current);
        }
    }

    private long occupancy()
    {
        return size() - (containsZero ? 1 : 0);
    }

    private long loadLimit()
    {
        return capacity() - (capacity() >>> 2);
    }

    private static long mix(long value)
    {
        long hash = value * 0x9E3779B97F4A7C15L;

        return hash ^ (hash >>> 32);
    }

    private static long tableSize(long expectedElements)
    {
        if (expectedElements < 0)
        {
            throw new IllegalArgumentException("expectedElements cannot be negative");
        }

        long needed = expectedElements + ((expectedElements + 2) / 3);

        return nextPowerOfTwo(Math.max(16, needed));
    }

    private static long nextPowerOfTwo(long value)
    {
        long highest = Long.highestOneBit(value);

        return highest == value ? value : highest << 1;
    }
}
