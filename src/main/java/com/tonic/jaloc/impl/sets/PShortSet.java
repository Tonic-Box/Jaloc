package com.tonic.jaloc.impl.sets;

import com.tonic.jaloc.impl.arrays.PLongArray;
import com.tonic.jaloc.impl.arrays.PLongWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractNativeCollection;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.Objects;
import java.util.function.IntConsumer;

public final class PShortSet extends AbstractNativeCollection<PLongArray, PLongWriter>
{
    private static final long WORDS = 1024;

    public PShortSet()
    {
        this(SystemAllocator.getInstance());
    }

    public PShortSet(NativeAllocator allocator)
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

    public boolean add(short value)
    {
        long index = value & 0xFFFFL;
        long word = index >>> 6;
        long mask = 1L << index;
        long current = elements().get(word);

        if ((current & mask) != 0)
        {
            return false;
        }

        elements().set(word, current | mask);
        size(size() + 1);
        return true;
    }

    public boolean remove(short value)
    {
        long index = value & 0xFFFFL;
        long word = index >>> 6;
        long mask = 1L << index;
        long current = elements().get(word);

        if ((current & mask) == 0)
        {
            return false;
        }

        elements().set(word, current & ~mask);
        size(size() - 1);
        return true;
    }

    public boolean contains(short value)
    {
        long index = value & 0xFFFFL;

        return (elements().get(index >>> 6) & (1L << index)) != 0;
    }

    public void forEach(IntConsumer consumer)
    {
        Objects.requireNonNull(consumer, "consumer");

        long words = capacity();

        for (long word = 0; word < words; word++)
        {
            long current = elements().get(word);

            while (current != 0)
            {
                long bit = Long.numberOfTrailingZeros(current);

                consumer.accept((short) ((word << 6) + bit));
                current &= current - 1;
            }
        }
    }

    public void clear()
    {
        ensureOpen();

        elements().clear();
        size(0);
    }
}
