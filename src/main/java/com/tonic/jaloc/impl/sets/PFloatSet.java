package com.tonic.jaloc.impl.sets;

import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.iface.FloatConsumer;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.Objects;

public final class PFloatSet implements AutoCloseable
{
    private final PIntSet bits;

    public PFloatSet()
    {
        this(0);
    }

    public PFloatSet(long expectedElements)
    {
        this(SystemAllocator.getInstance(), expectedElements);
    }

    public PFloatSet(NativeAllocator allocator, long expectedElements)
    {
        this.bits = new PIntSet(allocator, expectedElements);
    }

    public boolean add(float value)
    {
        return bits.add(Float.floatToIntBits(value));
    }

    public boolean remove(float value)
    {
        return bits.remove(Float.floatToIntBits(value));
    }

    public boolean contains(float value)
    {
        return bits.contains(Float.floatToIntBits(value));
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

    public void forEach(FloatConsumer consumer)
    {
        Objects.requireNonNull(consumer, "consumer");

        bits.forEach(value -> consumer.accept(Float.intBitsToFloat(value)));
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
