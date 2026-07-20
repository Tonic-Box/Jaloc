package com.tonic.jaloc.impl.sets;

import com.tonic.jaloc.impl.arrays.PLongArray;
import com.tonic.jaloc.impl.arrays.PLongWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractNativeCollection;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

import java.util.Objects;

public final class PBitSet extends AbstractNativeCollection<PLongArray, PLongWriter>
{
    private long wordsBase;
    private long wordCapacity;

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

        this.wordsBase = elementsBaseAddress();
        this.wordCapacity = capacity();
    }

    @Override
    protected PLongArray createArray(NativeAllocator allocator, long capacity)
    {
        return new PLongArray(allocator, capacity);
    }

    @Override
    protected void migrateElements(PLongArray source, PLongArray destination)
    {
        long words = sizeUnchecked();

        for (long i = 0; i < words; i++)
        {
            destination.setUnchecked(i, source.getUnchecked(i));
        }
    }

    public void set(long bit)
    {
        ensureOpen();

        long word = wordIndex(bit);

        if (word >= wordCapacity)
        {
            growWords(word + 1);
        }

        long address = wordsBase + (word << 3);

        UnsafeMemory.putLong(address, UnsafeMemory.getLong(address) | (1L << bit));

        if (word >= sizeUnchecked())
        {
            size(word + 1);
        }
    }

    public void clear(long bit)
    {
        ensureOpen();

        long word = wordIndex(bit);

        if (word >= sizeUnchecked())
        {
            return;
        }

        long address = wordsBase + (word << 3);

        UnsafeMemory.putLong(address, UnsafeMemory.getLong(address) & ~(1L << bit));
    }

    public void flip(long bit)
    {
        ensureOpen();

        long word = wordIndex(bit);

        if (word >= wordCapacity)
        {
            growWords(word + 1);
        }

        long address = wordsBase + (word << 3);

        UnsafeMemory.putLong(address, UnsafeMemory.getLong(address) ^ (1L << bit));

        if (word >= sizeUnchecked())
        {
            size(word + 1);
        }
    }

    public boolean get(long bit)
    {
        ensureOpen();

        long word = wordIndex(bit);

        if (word >= sizeUnchecked())
        {
            return false;
        }

        return (UnsafeMemory.getLong(wordsBase + (word << 3)) & (1L << bit)) != 0;
    }

    public long cardinality()
    {
        ensureOpen();

        long words = sizeUnchecked();
        long count = 0;

        for (long i = 0; i < words; i++)
        {
            count += Long.bitCount(UnsafeMemory.getLong(wordsBase + (i << 3)));
        }

        return count;
    }

    public long length()
    {
        ensureOpen();

        for (long word = sizeUnchecked() - 1; word >= 0; word--)
        {
            long value = UnsafeMemory.getLong(wordsBase + (word << 3));

            if (value != 0)
            {
                return (word << 6) + (64 - Long.numberOfLeadingZeros(value));
            }
        }

        return 0;
    }

    public long nextSetBit(long fromBit)
    {
        ensureOpen();

        long word = wordIndex(fromBit);
        long words = sizeUnchecked();

        if (word >= words)
        {
            return -1;
        }

        long current = UnsafeMemory.getLong(wordsBase + (word << 3)) & (-1L << fromBit);

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

            current = UnsafeMemory.getLong(wordsBase + (word << 3));
        }
    }

    public void and(PBitSet other)
    {
        Objects.requireNonNull(other, "other");
        ensureOpen();

        long words = sizeUnchecked();
        long otherWords = other.size();

        for (long i = 0; i < words; i++)
        {
            long value = i < otherWords ? UnsafeMemory.getLong(other.wordsBase + (i << 3)) : 0;
            long address = wordsBase + (i << 3);

            UnsafeMemory.putLong(address, UnsafeMemory.getLong(address) & value);
        }
    }

    public void or(PBitSet other)
    {
        Objects.requireNonNull(other, "other");
        ensureOpen();

        long otherWords = other.size();

        if (otherWords > wordCapacity)
        {
            growWords(otherWords);
        }

        for (long i = 0; i < otherWords; i++)
        {
            long address = wordsBase + (i << 3);

            UnsafeMemory.putLong(address, UnsafeMemory.getLong(address) | UnsafeMemory.getLong(other.wordsBase + (i << 3)));
        }

        if (otherWords > sizeUnchecked())
        {
            size(otherWords);
        }
    }

    public void xor(PBitSet other)
    {
        Objects.requireNonNull(other, "other");
        ensureOpen();

        long otherWords = other.size();

        if (otherWords > wordCapacity)
        {
            growWords(otherWords);
        }

        for (long i = 0; i < otherWords; i++)
        {
            long address = wordsBase + (i << 3);

            UnsafeMemory.putLong(address, UnsafeMemory.getLong(address) ^ UnsafeMemory.getLong(other.wordsBase + (i << 3)));
        }

        if (otherWords > sizeUnchecked())
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

    private void growWords(long requiredWords)
    {
        replaceArray(growCapacity(wordCapacity, requiredWords));

        wordsBase = elementsBaseAddress();
        wordCapacity = capacity();
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
