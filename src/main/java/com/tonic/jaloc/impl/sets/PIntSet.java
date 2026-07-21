package com.tonic.jaloc.impl.sets;

import com.tonic.jaloc.impl.arrays.PIntArray;
import com.tonic.jaloc.impl.arrays.PIntWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractNativeCollection;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.Objects;
import java.util.function.IntConsumer;

/**
 * A growable native int hash set with open addressing and backward-shift deletion.
 */
public final class PIntSet extends AbstractNativeCollection<PIntArray, PIntWriter>
{
    private boolean containsZero;

    /**
     * Creates an empty set on the system allocator.
     */
    public PIntSet()
    {
        this(0);
    }

    /**
     * Creates an empty set presized for expectedElements on the system allocator.
     *
     * @param expectedElements presizes the table
     * @throws IllegalArgumentException if expectedElements is negative
     */
    public PIntSet(long expectedElements)
    {
        this(SystemAllocator.getInstance(), expectedElements);
    }

    /**
     * Creates an empty set presized for expectedElements on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param expectedElements presizes the table
     * @throws IllegalArgumentException if expectedElements is negative
     */
    public PIntSet(NativeAllocator allocator, long expectedElements)
    {
        super(allocator, new PIntArray(allocator, tableSize(expectedElements)));
    }

    @Override
    protected PIntArray createArray(NativeAllocator allocator, long capacity)
    {
        return new PIntArray(allocator, capacity);
    }

    @Override
    protected void migrateElements(PIntArray source, PIntArray destination)
    {
        long sourceLength = source.length();
        long mask = destination.length() - 1;

        for (long i = 0; i < sourceLength; i++)
        {
            int value = source.getUnchecked(i);

            if (value == 0)
            {
                continue;
            }

            long position = mix(value) & mask;

            while (destination.getUnchecked(position) != 0)
            {
                position = (position + 1) & mask;
            }

            destination.setUnchecked(position, value);
        }
    }

    /**
     * Adds value.
     *
     * @param value the value to add
     * @return true if the set changed
     * @throws IllegalStateException if closed
     */
    public boolean add(int value)
    {
        ensureOpen();

        if (value == 0)
        {
            if (containsZero)
            {
                return false;
            }

            containsZero = true;
            size(sizeUnchecked() + 1);
            return true;
        }

        PIntArray table = elements();
        long mask = table.length() - 1;
        long position = mix(value) & mask;

        while (true)
        {
            int current = table.getUnchecked(position);

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

        if (occupancy() + 1 > loadLimit(mask + 1))
        {
            replaceArray((mask + 1) << 1);

            table = elements();
            mask = table.length() - 1;
            position = mix(value) & mask;

            while (table.getUnchecked(position) != 0)
            {
                position = (position + 1) & mask;
            }
        }

        table.setUnchecked(position, value);
        size(sizeUnchecked() + 1);
        return true;
    }

    /**
     * Removes value.
     *
     * @param value the value to remove
     * @return true if the set changed
     * @throws IllegalStateException if closed
     */
    public boolean remove(int value)
    {
        ensureOpen();

        if (value == 0)
        {
            if (!containsZero)
            {
                return false;
            }

            containsZero = false;
            size(sizeUnchecked() - 1);
            return true;
        }

        PIntArray table = elements();
        long mask = table.length() - 1;
        long position = mix(value) & mask;

        while (true)
        {
            int current = table.getUnchecked(position);

            if (current == 0)
            {
                return false;
            }

            if (current == value)
            {
                shiftKeys(table, position, mask);
                size(sizeUnchecked() - 1);
                return true;
            }

            position = (position + 1) & mask;
        }
    }

    /**
     * Tests whether value is present.
     *
     * @param value the value to test
     * @return true if present
     * @throws IllegalStateException if closed
     */
    public boolean contains(int value)
    {
        ensureOpen();

        if (value == 0)
        {
            return containsZero;
        }

        PIntArray table = elements();
        long mask = table.length() - 1;
        long position = mix(value) & mask;

        while (true)
        {
            int current = table.getUnchecked(position);

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

    /**
     * Emits each present value in no particular order.
     *
     * @param consumer the receiver
     * @throws NullPointerException if consumer is null
     * @throws IllegalStateException if closed
     */
    public void forEach(IntConsumer consumer)
    {
        Objects.requireNonNull(consumer, "consumer");
        ensureOpen();

        if (containsZero)
        {
            consumer.accept(0);
        }

        PIntArray table = elements();
        long slots = table.length();

        for (long i = 0; i < slots; i++)
        {
            int current = table.getUnchecked(i);

            if (current != 0)
            {
                consumer.accept(current);
            }
        }
    }

    /**
     * Empties the set.
     *
     * @throws IllegalStateException if closed
     */
    public void clear()
    {
        ensureOpen();

        elements().clear();
        containsZero = false;
        size(0);
    }

    private void shiftKeys(PIntArray table, long position, long mask)
    {
        while (true)
        {
            long last = position;

            position = (position + 1) & mask;

            int current;

            while (true)
            {
                current = table.getUnchecked(position);

                if (current == 0)
                {
                    table.setUnchecked(last, 0);
                    return;
                }

                long slot = mix(current) & mask;

                if (last <= position ? (last >= slot || slot > position) : (last >= slot && slot > position))
                {
                    break;
                }

                position = (position + 1) & mask;
            }

            table.setUnchecked(last, current);
        }
    }

    private long occupancy()
    {
        return sizeUnchecked() - (containsZero ? 1 : 0);
    }

    private long loadLimit(long tableLength)
    {
        return tableLength - (tableLength >>> 2);
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
