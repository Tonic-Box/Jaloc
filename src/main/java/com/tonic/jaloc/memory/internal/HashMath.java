package com.tonic.jaloc.memory.internal;

/**
 * Shared hash-table math for the open-addressing collections.
 */
public final class HashMath
{
    private HashMath()
    {
    }

    /**
     * Mixes key bits into a table hash.
     *
     * @param value the key bits
     * @return the mixed hash
     */
    public static long mix(long value)
    {
        long hash = value * 0x9E3779B97F4A7C15L;

        return hash ^ (hash >>> 32);
    }

    /**
     * @param value the value to round
     * @return value rounded up to a power of two
     */
    public static long nextPowerOfTwo(long value)
    {
        long highest = Long.highestOneBit(value);

        return highest == value ? value : highest << 1;
    }

    /**
     * Sizes a power-of-two table for expectedElements at the shared load factor.
     *
     * @param expectedElements the element count to presize for
     * @return the table length
     * @throws IllegalArgumentException if expectedElements is negative
     */
    public static long tableSize(long expectedElements)
    {
        if (expectedElements < 0)
        {
            throw new IllegalArgumentException("expectedElements cannot be negative");
        }

        long needed = expectedElements + ((expectedElements + 2) / 3);

        return nextPowerOfTwo(Math.max(16, needed));
    }

    /**
     * @param tableSize the table length
     * @return the occupancy limit before growth
     */
    public static long loadLimit(long tableSize)
    {
        return tableSize - (tableSize >>> 2);
    }
}
