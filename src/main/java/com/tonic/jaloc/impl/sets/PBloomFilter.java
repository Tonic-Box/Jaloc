package com.tonic.jaloc.impl.sets;

import com.tonic.jaloc.impl.arrays.PLongArray;
import com.tonic.jaloc.impl.arrays.PLongWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractNativeCollection;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.HashMath;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

/**
 * A fixed-size native Bloom filter; mightContain can return false positives but never false negatives.
 */
public final class PBloomFilter extends AbstractNativeCollection<PLongArray, PLongWriter>
{
    private final long bitCount;
    private final int hashCount;
    private final long wordsBase;

    /**
     * Sizes a filter for expectedElements at falsePositiveRate on the system allocator.
     *
     * @param expectedElements the element count to size for
     * @param falsePositiveRate the target false-positive probability
     * @throws IllegalArgumentException if expectedElements is not positive or falsePositiveRate is not between 0 and 1
     */
    public PBloomFilter(long expectedElements, double falsePositiveRate)
    {
        this(SystemAllocator.getInstance(), expectedElements, falsePositiveRate);
    }

    /**
     * Sizes a filter for expectedElements at falsePositiveRate on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param expectedElements the element count to size for
     * @param falsePositiveRate the target false-positive probability
     * @throws IllegalArgumentException if expectedElements is not positive or falsePositiveRate is not between 0 and 1
     */
    public PBloomFilter(NativeAllocator allocator, long expectedElements, double falsePositiveRate)
    {
        super(allocator, new PLongArray(allocator, wordCount(expectedElements, falsePositiveRate)));

        this.bitCount = capacity() << 6;
        this.hashCount = hashCount(expectedElements, bitCount);
        this.wordsBase = elementsBaseAddress();
        size(capacity());
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
            destination.setUnchecked(i, source.getUnchecked(i));
        }
    }

    /**
     * Records value.
     *
     * @param value the value to record
     * @throws IllegalStateException if closed
     */
    public void add(long value)
    {
        ensureOpen();

        long hash = HashMath.mix(value);
        long step = HashMath.mix(hash) | 1L;

        for (int i = 0; i < hashCount; i++)
        {
            long bit = ((hash + i * step) & Long.MAX_VALUE) % bitCount;
            long address = wordsBase + ((bit >>> 6) << 3);

            UnsafeMemory.putLong(address, UnsafeMemory.getLong(address) | (1L << bit));
        }
    }

    /**
     * Tests whether value may have been recorded.
     *
     * @param value the value to test
     * @return true if value was possibly recorded, false if definitely not
     * @throws IllegalStateException if closed
     */
    public boolean mightContain(long value)
    {
        ensureOpen();

        long hash = HashMath.mix(value);
        long step = HashMath.mix(hash) | 1L;

        for (int i = 0; i < hashCount; i++)
        {
            long bit = ((hash + i * step) & Long.MAX_VALUE) % bitCount;

            if ((UnsafeMemory.getLong(wordsBase + ((bit >>> 6) << 3)) & (1L << bit)) == 0)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Empties the filter.
     *
     * @throws IllegalStateException if closed
     */
    public void clear()
    {
        ensureOpen();

        elements().clear();
    }

    /**
     * @return the filter width in bits
     * @throws IllegalStateException if closed
     */
    public long bitCount()
    {
        ensureOpen();

        return bitCount;
    }

    /**
     * @return the number of hash probes per value
     * @throws IllegalStateException if closed
     */
    public int hashCount()
    {
        ensureOpen();

        return hashCount;
    }

    private static long wordCount(long expectedElements, double falsePositiveRate)
    {
        if (expectedElements <= 0)
        {
            throw new IllegalArgumentException("expectedElements must be positive");
        }

        if (!(falsePositiveRate > 0.0D && falsePositiveRate < 1.0D))
        {
            throw new IllegalArgumentException("falsePositiveRate must be between 0 and 1");
        }

        long bits = (long) Math.ceil(-expectedElements * Math.log(falsePositiveRate) / (Math.log(2.0D) * Math.log(2.0D)));

        return Math.max(1, (bits + 63) >>> 6);
    }

    private static int hashCount(long expectedElements, long bitCount)
    {
        return Math.max(1, (int) Math.round((double) bitCount / expectedElements * Math.log(2.0D)));
    }
}
