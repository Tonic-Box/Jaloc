package com.tonic.jaloc.impl.sets;

import com.tonic.jaloc.impl.arrays.PLongArray;
import com.tonic.jaloc.impl.arrays.PLongWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractNativeCollection;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.Objects;
import java.util.function.IntConsumer;

/**
 * A byte set over a fixed 256-bit direct-indexed bitmap.
 */
public final class PByteSet extends AbstractNativeCollection<PLongArray, PLongWriter>
{
    private static final long WORDS = 4;

    /**
     * Creates an empty set on the system allocator.
     */
    public PByteSet()
    {
        this(SystemAllocator.getInstance());
    }

    /**
     * Creates an empty set on the given allocator.
     *
     * @param allocator the allocator to source memory from
     */
    public PByteSet(NativeAllocator allocator)
    {
        super(allocator, new PLongArray(allocator, WORDS));
    }

    @Override
    protected PLongArray createArray(NativeAllocator allocator, long capacity)
    {
        return new PLongArray(allocator, capacity);
    }

    @Override
    protected void migrateElements(PLongArray source, PLongArray destination)
    {
        long words = source.length();

        for (long i = 0; i < words; i++)
        {
            destination.set(i, source.get(i));
        }
    }

    /**
     * Adds value.
     *
     * @param value the value to add
     * @return true if the set changed
     * @throws IllegalStateException if closed
     */
    public boolean add(byte value)
    {
        long index = value & 0xFFL;
        long word = index >>> 6;
        long mask = 1L << index;
        long current = elements().getUnchecked(word);

        if ((current & mask) != 0)
        {
            return false;
        }

        elements().setUnchecked(word, current | mask);
        size(size() + 1);
        return true;
    }

    /**
     * Removes value.
     *
     * @param value the value to remove
     * @return true if the set changed
     * @throws IllegalStateException if closed
     */
    public boolean remove(byte value)
    {
        long index = value & 0xFFL;
        long word = index >>> 6;
        long mask = 1L << index;
        long current = elements().getUnchecked(word);

        if ((current & mask) == 0)
        {
            return false;
        }

        elements().setUnchecked(word, current & ~mask);
        size(size() - 1);
        return true;
    }

    /**
     * Tests whether value is present.
     *
     * @param value the value to test
     * @return true if present
     * @throws IllegalStateException if closed
     */
    public boolean contains(byte value)
    {
        long index = value & 0xFFL;

        return (elements().getUnchecked(index >>> 6) & (1L << index)) != 0;
    }

    /**
     * Emits each present value in ascending order.
     *
     * @param consumer the receiver
     * @throws NullPointerException if consumer is null
     * @throws IllegalStateException if closed
     */
    public void forEach(IntConsumer consumer)
    {
        Objects.requireNonNull(consumer, "consumer");

        PLongArray table = elements();
        long words = table.length();

        for (long word = 0; word < words; word++)
        {
            long current = table.getUnchecked(word);

            while (current != 0)
            {
                long bit = Long.numberOfTrailingZeros(current);

                consumer.accept((byte) ((word << 6) + bit));
                current &= current - 1;
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
        size(0);
    }
}
