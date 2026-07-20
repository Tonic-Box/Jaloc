package com.tonic.jaloc.impl.sets;

import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.Objects;
import java.util.function.DoubleConsumer;

public final class PDoubleSet implements AutoCloseable
{
    private final PLongSet bits;

    public PDoubleSet()
    {
        this(0);
    }

    public PDoubleSet(long expectedElements)
    {
        this(SystemAllocator.getInstance(), expectedElements);
    }

    public PDoubleSet(NativeAllocator allocator, long expectedElements)
    {
        this.bits = new PLongSet(allocator, expectedElements);
    }

    public boolean add(double value)
    {
        return bits.add(Double.doubleToLongBits(value));
    }

    public boolean remove(double value)
    {
        return bits.remove(Double.doubleToLongBits(value));
    }

    public boolean contains(double value)
    {
        return bits.contains(Double.doubleToLongBits(value));
    }

    public long size()
    {
        return bits.size();
    }

    public boolean isEmpty()
    {
        return bits.isEmpty();
    }

    public boolean isOpen()
    {
        return bits.isOpen();
    }

    public void forEach(DoubleConsumer consumer)
    {
        Objects.requireNonNull(consumer, "consumer");

        bits.forEach(value -> consumer.accept(Double.longBitsToDouble(value)));
    }

    public void clear()
    {
        bits.clear();
    }

    @Override
    public void close()
    {
        bits.close();
    }
}
