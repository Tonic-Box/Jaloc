package com.tonic.jaloc.impl.sets;

import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.Objects;
import java.util.function.DoubleConsumer;

/**
 * A double set over bit patterns in a PLongSet; NaNs collapse to one element, -0.0 and 0.0 stay distinct.
 */
public final class PDoubleSet implements AutoCloseable
{
    private final PLongSet bits;

    /**
     * Creates an empty set on the system allocator.
     */
    public PDoubleSet()
    {
        this(0);
    }

    /**
     * Creates an empty set presized for expectedElements on the system allocator.
     *
     * @param expectedElements presizes the table
     * @throws IllegalArgumentException if expectedElements is negative
     */
    public PDoubleSet(long expectedElements)
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
    public PDoubleSet(NativeAllocator allocator, long expectedElements)
    {
        this.bits = new PLongSet(allocator, expectedElements);
    }

    /**
     * Adds value.
     *
     * @param value the value to add
     * @return true if the set changed
     * @throws IllegalStateException if closed
     */
    public boolean add(double value)
    {
        return bits.add(Double.doubleToLongBits(value));
    }

    /**
     * Removes value.
     *
     * @param value the value to remove
     * @return true if the set changed
     * @throws IllegalStateException if closed
     */
    public boolean remove(double value)
    {
        return bits.remove(Double.doubleToLongBits(value));
    }

    /**
     * Tests whether value is present.
     *
     * @param value the value to test
     * @return true if present
     * @throws IllegalStateException if closed
     */
    public boolean contains(double value)
    {
        return bits.contains(Double.doubleToLongBits(value));
    }

    /**
     * @return the element count
     */
    public long size()
    {
        return bits.size();
    }

    /**
     * @return true if no elements are present
     */
    public boolean isEmpty()
    {
        return bits.isEmpty();
    }

    /**
     * @return true until closed
     */
    public boolean isOpen()
    {
        return bits.isOpen();
    }

    /**
     * Emits each present value in no particular order.
     *
     * @param consumer the receiver
     * @throws NullPointerException if consumer is null
     * @throws IllegalStateException if closed
     */
    public void forEach(DoubleConsumer consumer)
    {
        Objects.requireNonNull(consumer, "consumer");

        bits.forEach(value -> consumer.accept(Double.longBitsToDouble(value)));
    }

    /**
     * Empties the set.
     *
     * @throws IllegalStateException if closed
     */
    public void clear()
    {
        bits.clear();
    }

    /**
     * Releases the backing native memory. Safe to call more than once.
     */
    @Override
    public void close()
    {
        bits.close();
    }
}
