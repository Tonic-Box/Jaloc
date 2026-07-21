package com.tonic.jaloc.impl.sets;

import com.tonic.jaloc.impl.arrays.PLongArray;
import com.tonic.jaloc.impl.arrays.PLongWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractNativeCollection;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

import java.util.Objects;
import java.util.function.LongConsumer;

/**
 * A growable native bitset over 64-bit words; reads past the end are false.
 */
public final class PBitSet extends AbstractNativeCollection<PLongArray, PLongWriter>
{
    private long wordsBase;
    private long wordCapacity;

    /**
     * Creates an empty bitset on the system allocator.
     */
    public PBitSet()
    {
        this(0);
    }

    /**
     * Creates a bitset presized for nbits on the system allocator.
     *
     * @param nbits presizes the word storage
     * @throws IllegalArgumentException if nbits is negative
     */
    public PBitSet(long nbits)
    {
        this(SystemAllocator.getInstance(), nbits);
    }

    /**
     * Creates a bitset presized for nbits on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param nbits presizes the word storage
     * @throws IllegalArgumentException if nbits is negative
     */
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

    /**
     * Sets bit, growing if needed.
     *
     * @param bit the bit index
     * @throws IndexOutOfBoundsException if bit is negative
     * @throws IllegalStateException if closed
     */
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

    /**
     * Clears bit; past the end is a no-op.
     *
     * @param bit the bit index
     * @throws IndexOutOfBoundsException if bit is negative
     * @throws IllegalStateException if closed
     */
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

    /**
     * Flips bit, growing if needed.
     *
     * @param bit the bit index
     * @throws IndexOutOfBoundsException if bit is negative
     * @throws IllegalStateException if closed
     */
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

    /**
     * Sets fromBit inclusive to toBit exclusive, growing if needed.
     *
     * @param fromBit the range start, inclusive
     * @param toBit the range end, exclusive
     * @throws IndexOutOfBoundsException if fromBit is negative or toBit is less than fromBit
     * @throws IllegalStateException if closed
     */
    public void set(long fromBit, long toBit)
    {
        ensureOpen();

        if (fromBit < 0 || toBit < fromBit)
        {
            throw new IndexOutOfBoundsException("fromBit=" + fromBit + ", toBit=" + toBit);
        }

        if (fromBit == toBit)
        {
            return;
        }

        long firstWord = fromBit >>> 6;
        long lastWord = (toBit - 1) >>> 6;

        if (lastWord >= wordCapacity)
        {
            growWords(lastWord + 1);
        }

        long firstMask = -1L << (fromBit & 63);
        long lastMask = (toBit & 63) == 0 ? -1L : (1L << (toBit & 63)) - 1;

        if (firstWord == lastWord)
        {
            long address = wordsBase + (firstWord << 3);

            UnsafeMemory.putLong(address, UnsafeMemory.getLong(address) | (firstMask & lastMask));
        }
        else
        {
            long firstAddress = wordsBase + (firstWord << 3);

            UnsafeMemory.putLong(firstAddress, UnsafeMemory.getLong(firstAddress) | firstMask);

            if (lastWord - firstWord > 1)
            {
                UnsafeMemory.fill(firstAddress + Long.BYTES, (lastWord - firstWord - 1) << 3, (byte) 0xFF);
            }

            long lastAddress = wordsBase + (lastWord << 3);

            UnsafeMemory.putLong(lastAddress, UnsafeMemory.getLong(lastAddress) | lastMask);
        }

        if (lastWord >= sizeUnchecked())
        {
            size(lastWord + 1);
        }
    }

    /**
     * Clears fromBit inclusive to toBit exclusive; past the end is a no-op.
     *
     * @param fromBit the range start, inclusive
     * @param toBit the range end, exclusive
     * @throws IndexOutOfBoundsException if fromBit is negative or toBit is less than fromBit
     * @throws IllegalStateException if closed
     */
    public void clear(long fromBit, long toBit)
    {
        ensureOpen();

        if (fromBit < 0 || toBit < fromBit)
        {
            throw new IndexOutOfBoundsException("fromBit=" + fromBit + ", toBit=" + toBit);
        }

        long to = Math.min(toBit, sizeUnchecked() << 6);

        if (fromBit >= to)
        {
            return;
        }

        long firstWord = fromBit >>> 6;
        long lastWord = (to - 1) >>> 6;
        long firstMask = -1L << (fromBit & 63);
        long lastMask = (to & 63) == 0 ? -1L : (1L << (to & 63)) - 1;

        if (firstWord == lastWord)
        {
            long address = wordsBase + (firstWord << 3);

            UnsafeMemory.putLong(address, UnsafeMemory.getLong(address) & ~(firstMask & lastMask));
        }
        else
        {
            long firstAddress = wordsBase + (firstWord << 3);

            UnsafeMemory.putLong(firstAddress, UnsafeMemory.getLong(firstAddress) & ~firstMask);

            if (lastWord - firstWord > 1)
            {
                UnsafeMemory.clear(firstAddress + Long.BYTES, (lastWord - firstWord - 1) << 3);
            }

            long lastAddress = wordsBase + (lastWord << 3);

            UnsafeMemory.putLong(lastAddress, UnsafeMemory.getLong(lastAddress) & ~lastMask);
        }
    }

    /**
     * Flips fromBit inclusive to toBit exclusive, growing if needed.
     *
     * @param fromBit the range start, inclusive
     * @param toBit the range end, exclusive
     * @throws IndexOutOfBoundsException if fromBit is negative or toBit is less than fromBit
     * @throws IllegalStateException if closed
     */
    public void flip(long fromBit, long toBit)
    {
        ensureOpen();

        if (fromBit < 0 || toBit < fromBit)
        {
            throw new IndexOutOfBoundsException("fromBit=" + fromBit + ", toBit=" + toBit);
        }

        if (fromBit == toBit)
        {
            return;
        }

        long firstWord = fromBit >>> 6;
        long lastWord = (toBit - 1) >>> 6;

        if (lastWord >= wordCapacity)
        {
            growWords(lastWord + 1);
        }

        long firstMask = -1L << (fromBit & 63);
        long lastMask = (toBit & 63) == 0 ? -1L : (1L << (toBit & 63)) - 1;

        if (firstWord == lastWord)
        {
            long address = wordsBase + (firstWord << 3);

            UnsafeMemory.putLong(address, UnsafeMemory.getLong(address) ^ (firstMask & lastMask));
        }
        else
        {
            long firstAddress = wordsBase + (firstWord << 3);

            UnsafeMemory.putLong(firstAddress, UnsafeMemory.getLong(firstAddress) ^ firstMask);

            for (long word = firstWord + 1; word < lastWord; word++)
            {
                long address = wordsBase + (word << 3);

                UnsafeMemory.putLong(address, ~UnsafeMemory.getLong(address));
            }

            long lastAddress = wordsBase + (lastWord << 3);

            UnsafeMemory.putLong(lastAddress, UnsafeMemory.getLong(lastAddress) ^ lastMask);
        }

        if (lastWord >= sizeUnchecked())
        {
            size(lastWord + 1);
        }
    }

    /**
     * Reads bit; past the end is false.
     *
     * @param bit the bit index
     * @return the bit
     * @throws IndexOutOfBoundsException if bit is negative
     * @throws IllegalStateException if closed
     */
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

    /**
     * @return the number of set bits
     * @throws IllegalStateException if closed
     */
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

    /**
     * @return the highest set bit plus one, or zero when empty
     * @throws IllegalStateException if closed
     */
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

    /**
     * Finds the first set bit at or after fromBit.
     *
     * @param fromBit the search start
     * @return the bit index, or -1 if none
     * @throws IndexOutOfBoundsException if fromBit is negative
     * @throws IllegalStateException if closed
     */
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

    /**
     * Emits every set bit in ascending order.
     *
     * @param consumer the receiver
     * @throws NullPointerException if consumer is null
     * @throws IllegalStateException if closed
     */
    public void forEachSetBit(LongConsumer consumer)
    {
        Objects.requireNonNull(consumer, "consumer");
        ensureOpen();

        long words = sizeUnchecked();

        for (long word = 0; word < words; word++)
        {
            long current = UnsafeMemory.getLong(wordsBase + (word << 3));

            while (current != 0)
            {
                consumer.accept((word << 6) + Long.numberOfTrailingZeros(current));
                current &= current - 1;
            }
        }
    }

    /**
     * Intersects this bitset with other.
     *
     * @param other the other bitset
     * @throws NullPointerException if other is null
     * @throws IllegalStateException if either bitset is closed
     */
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

    /**
     * Unions this bitset with other, growing if needed.
     *
     * @param other the other bitset
     * @throws NullPointerException if other is null
     * @throws IllegalStateException if either bitset is closed
     */
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

    /**
     * Exclusive-ors this bitset with other, growing if needed.
     *
     * @param other the other bitset
     * @throws NullPointerException if other is null
     * @throws IllegalStateException if either bitset is closed
     */
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

    /**
     * Clears every bit.
     *
     * @throws IllegalStateException if closed
     */
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
