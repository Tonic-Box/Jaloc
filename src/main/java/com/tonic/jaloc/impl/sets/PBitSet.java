package com.tonic.jaloc.impl.sets;

import com.tonic.jaloc.impl.arrays.PLongArray;
import com.tonic.jaloc.impl.arrays.PLongWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractNativeCollection;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.Objects;

public final class PBitSet extends AbstractNativeCollection<PLongArray, PLongWriter>
{
    public PBitSet()
    {
        this(0);
    }

    public PBitSet(long nbits)
    {
        this(SystemAllocator.getInstance(), nbits);
    }

    public PBitSet(NativeAllocator allocator, long nbits)
    {
        super(allocator, new PLongArray(allocator, wordCount(requireBitCount(nbits))));
    }

    @Override
    protected PLongArray createArray(NativeAllocator allocator, long capacity)
    {
        return new PLongArray(allocator, capacity);
    }

    @Override
    protected void migrateElements(PLongArray source, PLongArray destination)
    {
        long words = size();

        for (long i = 0; i < words; i++)
        {
            destination.setUnchecked(i, source.getUnchecked(i));
        }
    }

    public void set(long bit)
    {
        long word = wordIndex(bit);

        ensureWordCapacity(word + 1);

        elements().setUnchecked(word, elements().getUnchecked(word) | (1L << bit));

        if (word >= size())
        {
            size(word + 1);
        }
    }

    public void clear(long bit)
    {
        long word = wordIndex(bit);

        if (word >= size())
        {
            return;
        }

        elements().setUnchecked(word, elements().getUnchecked(word) & ~(1L << bit));
    }

    public void flip(long bit)
    {
        long word = wordIndex(bit);

        ensureWordCapacity(word + 1);

        elements().setUnchecked(word, elements().getUnchecked(word) ^ (1L << bit));

        if (word >= size())
        {
            size(word + 1);
        }
    }

    public boolean get(long bit)
    {
        long word = wordIndex(bit);

        if (word >= size())
        {
            return false;
        }

        return (elements().getUnchecked(word) & (1L << bit)) != 0;
    }

    public long cardinality()
    {
        long words = size();
        PLongArray table = elements();
        long count = 0;

        for (long i = 0; i < words; i++)
        {
            count += Long.bitCount(table.getUnchecked(i));
        }

        return count;
    }

    public long length()
    {
        PLongArray table = elements();

        for (long word = size() - 1; word >= 0; word--)
        {
            long value = table.getUnchecked(word);

            if (value != 0)
            {
                return (word << 6) + (64 - Long.numberOfLeadingZeros(value));
            }
        }

        return 0;
    }

    public long nextSetBit(long fromBit)
    {
        long word = wordIndex(fromBit);
        long words = size();

        if (word >= words)
        {
            return -1;
        }

        PLongArray table = elements();
        long current = table.getUnchecked(word) & (-1L << fromBit);

        while (true)
        {
            if (current != 0)
            {
                return (word << 6) + Long.numberOfTrailingZeros(current);
            }

            word++;

            if (word == words)
            {
                return -1;
            }

            current = table.getUnchecked(word);
        }
    }

    public void and(PBitSet other)
    {
        Objects.requireNonNull(other, "other");

        long words = size();
        long otherWords = other.size();
        PLongArray table = elements();
        PLongArray otherTable = other.elements();

        for (long i = 0; i < words; i++)
        {
            long value = i < otherWords ? otherTable.getUnchecked(i) : 0;

            table.setUnchecked(i, table.getUnchecked(i) & value);
        }
    }

    public void or(PBitSet other)
    {
        Objects.requireNonNull(other, "other");

        long otherWords = other.size();

        ensureWordCapacity(otherWords);

        PLongArray table = elements();
        PLongArray otherTable = other.elements();

        for (long i = 0; i < otherWords; i++)
        {
            table.setUnchecked(i, table.getUnchecked(i) | otherTable.getUnchecked(i));
        }

        if (otherWords > size())
        {
            size(otherWords);
        }
    }

    public void xor(PBitSet other)
    {
        Objects.requireNonNull(other, "other");

        long otherWords = other.size();

        ensureWordCapacity(otherWords);

        PLongArray table = elements();
        PLongArray otherTable = other.elements();

        for (long i = 0; i < otherWords; i++)
        {
            table.setUnchecked(i, table.getUnchecked(i) ^ otherTable.getUnchecked(i));
        }

        if (otherWords > size())
        {
            size(otherWords);
        }
    }

    public void clear()
    {
        ensureOpen();

        elements().clear();
        size(0);
    }

    private void ensureWordCapacity(long requiredWords)
    {
        ensureOpen();

        if (requiredWords <= capacity())
        {
            return;
        }

        replaceArray(growCapacity(capacity(), requiredWords));
    }

    private static long wordIndex(long bit)
    {
        if (bit < 0)
        {
            throw new IndexOutOfBoundsException("bit=" + bit);
        }

        return bit >>> 6;
    }

    private static long requireBitCount(long nbits)
    {
        if (nbits < 0)
        {
            throw new IllegalArgumentException("nbits cannot be negative");
        }

        return nbits;
    }

    private static long wordCount(long nbits)
    {
        return (nbits >>> 6) + ((nbits & 63L) == 0 ? 0 : 1);
    }
}
