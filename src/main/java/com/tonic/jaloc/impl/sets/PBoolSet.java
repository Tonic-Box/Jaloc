package com.tonic.jaloc.impl.sets;

import com.tonic.jaloc.impl.arrays.PLongArray;
import com.tonic.jaloc.impl.arrays.PLongWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractNativeCollection;
import com.tonic.jaloc.memory.iface.NativeAllocator;

public final class PBoolSet extends AbstractNativeCollection<PLongArray, PLongWriter>
{
    private static final long WORDS = 1;

    public PBoolSet()
    {
        this(SystemAllocator.getInstance());
    }

    public PBoolSet(NativeAllocator allocator)
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

    public boolean add(boolean value)
    {
        long mask = 1L << (value ? 1 : 0);
        long current = elements().getUnchecked(0);

        if ((current & mask) != 0)
        {
            return false;
        }

        elements().setUnchecked(0, current | mask);
        size(size() + 1);
        return true;
    }

    public boolean remove(boolean value)
    {
        long mask = 1L << (value ? 1 : 0);
        long current = elements().getUnchecked(0);

        if ((current & mask) == 0)
        {
            return false;
        }

        elements().setUnchecked(0, current & ~mask);
        size(size() - 1);
        return true;
    }

    public boolean contains(boolean value)
    {
        return (elements().getUnchecked(0) & (1L << (value ? 1 : 0))) != 0;
    }

    public void clear()
    {
        ensureOpen();

        elements().clear();
        size(0);
    }
}
