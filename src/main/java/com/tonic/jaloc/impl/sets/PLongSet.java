package com.tonic.jaloc.impl.sets;

import com.tonic.jaloc.impl.arrays.PLongArray;
import com.tonic.jaloc.impl.arrays.PLongWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractNativeCollection;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.HashMath;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

import java.util.Objects;
import java.util.function.LongConsumer;

/**
 * A growable native long hash set with open addressing and backward-shift deletion.
 */
public final class PLongSet extends AbstractNativeCollection<PLongArray, PLongWriter>
{
    private boolean containsZero;
    private long tableBase;
    private long tableMask;
    private long growLimit;

    /**
     * Creates an empty set on the system allocator.
     */
    public PLongSet()
    {
        this(0);
    }

    /**
     * Creates an empty set presized for expectedElements on the system allocator.
     *
     * @param expectedElements presizes the table
     * @throws IllegalArgumentException if expectedElements is negative
     */
    public PLongSet(long expectedElements)
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
    public PLongSet(NativeAllocator allocator, long expectedElements)
    {
        super(allocator, new PLongArray(allocator, HashMath.tableSize(expectedElements)));

        this.tableBase = elementsBaseAddress();
        this.tableMask = elementsUnchecked().length() - 1;
        this.growLimit = HashMath.loadLimit(tableMask + 1);
    }

    @Override
    protected PLongArray createArray(NativeAllocator allocator, long capacity)
    {
        return new PLongArray(allocator, capacity);
    }

    @Override
    protected void onArrayReplaced()
    {
        super.onArrayReplaced();

        tableBase = elementsBaseAddress();
        tableMask = elementsUnchecked().length() - 1;
        growLimit = HashMath.loadLimit(tableMask + 1);
    }

    @Override
    protected void migrateElements(PLongArray source, PLongArray destination)
    {
        long sourceLength = source.length();
        long mask = destination.length() - 1;

        for (long i = 0; i < sourceLength; i++)
        {
            long value = source.getUnchecked(i);

            if (value == 0)
            {
                continue;
            }

            long position = HashMath.mix(value) & mask;

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
            size(sizeUnchecked() + 1);
            return true;
        }

        long base = tableBase;
        long mask = tableMask;
        long position = HashMath.mix(value) & mask;

        while (true)
        {
            long current = UnsafeMemory.getLong(base + (position << 3));

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

        if (occupancy() + 1 > growLimit)
        {
            replaceArray((mask + 1) << 1);

            base = tableBase;
            mask = tableMask;
            position = HashMath.mix(value) & mask;

            while (UnsafeMemory.getLong(base + (position << 3)) != 0)
            {
                position = (position + 1) & mask;
            }
        }

        UnsafeMemory.putLong(base + (position << 3), value);
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
            size(sizeUnchecked() - 1);
            return true;
        }

        long base = tableBase;
        long mask = tableMask;
        long position = HashMath.mix(value) & mask;

        while (true)
        {
            long current = UnsafeMemory.getLong(base + (position << 3));

            if (current == 0)
            {
                return false;
            }

            if (current == value)
            {
                shiftKeys(position);
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
    public boolean contains(long value)
    {
        ensureOpen();

        if (value == 0)
        {
            return containsZero;
        }

        long base = tableBase;
        long mask = tableMask;
        long position = HashMath.mix(value) & mask;

        while (true)
        {
            long current = UnsafeMemory.getLong(base + (position << 3));

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
    public void forEach(LongConsumer consumer)
    {
        Objects.requireNonNull(consumer, "consumer");
        ensureOpen();

        if (containsZero)
        {
            consumer.accept(0);
        }

        long base = tableBase;
        long slots = tableMask + 1;

        for (long i = 0; i < slots; i++)
        {
            long current = UnsafeMemory.getLong(base + (i << 3));

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

    private void shiftKeys(long position)
    {
        long base = tableBase;
        long mask = tableMask;

        while (true)
        {
            long last = position;

            position = (position + 1) & mask;

            long current;

            while (true)
            {
                current = UnsafeMemory.getLong(base + (position << 3));

                if (current == 0)
                {
                    UnsafeMemory.putLong(base + (last << 3), 0);
                    return;
                }

                long slot = HashMath.mix(current) & mask;

                if (last <= position ? (last >= slot || slot > position) : (last >= slot && slot > position))
                {
                    break;
                }

                position = (position + 1) & mask;
            }

            UnsafeMemory.putLong(base + (last << 3), current);
        }
    }

    private long occupancy()
    {
        return sizeUnchecked() - (containsZero ? 1 : 0);
    }

}
