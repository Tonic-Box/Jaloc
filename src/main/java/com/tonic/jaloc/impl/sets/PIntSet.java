package com.tonic.jaloc.impl.sets;

import com.tonic.jaloc.impl.arrays.PIntArray;
import com.tonic.jaloc.impl.arrays.PIntWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractNativeCollection;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.HashMath;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

import java.util.Objects;
import java.util.function.IntConsumer;

/**
 * A growable native int hash set with open addressing and backward-shift deletion.
 */
public final class PIntSet extends AbstractNativeCollection<PIntArray, PIntWriter>
{
    private boolean containsZero;
    private long tableBase;
    private long tableMask;
    private long growLimit;

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
        super(allocator, new PIntArray(allocator, HashMath.tableSize(expectedElements)));

        this.tableBase = elementsBaseAddress();
        this.tableMask = elementsUnchecked().length() - 1;
        this.growLimit = HashMath.loadLimit(tableMask + 1);
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

        long base = tableBase;
        long mask = tableMask;
        long position = HashMath.mix(value) & mask;

        while (true)
        {
            int current = UnsafeMemory.getInt(base + (position << 2));

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

            tableBase = elementsBaseAddress();
            tableMask = elementsUnchecked().length() - 1;
            growLimit = HashMath.loadLimit(tableMask + 1);

            base = tableBase;
            mask = tableMask;
            position = HashMath.mix(value) & mask;

            while (UnsafeMemory.getInt(base + (position << 2)) != 0)
            {
                position = (position + 1) & mask;
            }
        }

        UnsafeMemory.putInt(base + (position << 2), value);
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

        long base = tableBase;
        long mask = tableMask;
        long position = HashMath.mix(value) & mask;

        while (true)
        {
            int current = UnsafeMemory.getInt(base + (position << 2));

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
    public boolean contains(int value)
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
            int current = UnsafeMemory.getInt(base + (position << 2));

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

        long base = tableBase;
        long slots = tableMask + 1;

        for (long i = 0; i < slots; i++)
        {
            int current = UnsafeMemory.getInt(base + (i << 2));

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

            int current;

            while (true)
            {
                current = UnsafeMemory.getInt(base + (position << 2));

                if (current == 0)
                {
                    UnsafeMemory.putInt(base + (last << 2), 0);
                    return;
                }

                long slot = HashMath.mix(current) & mask;

                if (last <= position ? (last >= slot || slot > position) : (last >= slot && slot > position))
                {
                    break;
                }

                position = (position + 1) & mask;
            }

            UnsafeMemory.putInt(base + (last << 2), current);
        }
    }

    private long occupancy()
    {
        return sizeUnchecked() - (containsZero ? 1 : 0);
    }

}
