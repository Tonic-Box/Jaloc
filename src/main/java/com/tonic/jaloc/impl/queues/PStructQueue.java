package com.tonic.jaloc.impl.queues;


import com.tonic.jaloc.impl.arrays.struct.*;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.*;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public final class PStructQueue<T extends PStruct> extends AbstractNativeRing<PStructArray<T>, PStructWriter<T>>
{
    private final StructLayout layout;
    private final StructViewFactory<T> viewFactory;

    public PStructQueue(StructLayout layout, StructViewFactory<T> viewFactory)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, 0);
    }

    public PStructQueue(StructLayout layout, StructViewFactory<T> viewFactory, long initialCapacity)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, initialCapacity);
    }

    public PStructQueue(NativeAllocator allocator, StructLayout layout, StructViewFactory<T> viewFactory, long initialCapacity)
    {
        super(Objects.requireNonNull(allocator, "allocator"), new PStructArray<>(allocator, viewFactory, layout, initialCapacity));

        this.layout = layout;
        this.viewFactory = viewFactory;
    }

    public StructLayout layout()
    {
        return layout;
    }

    @Override
    protected PStructArray<T> createArray(NativeAllocator allocator, long capacity)
    {
        return new PStructArray<>(allocator, viewFactory, layout, capacity);
    }

    public void ensureCapacity(long requiredCapacity)
    {
        ensureRingCapacity(requiredCapacity);
    }

    public void trimToSize()
    {
        trimRing();
    }

    public T enqueue()
    {
        long index = reserveTail();
        T struct = elements().at(index);

        commitTail();
        return struct;
    }

    public T enqueue(Consumer<? super T> initializer)
    {
        Objects.requireNonNull(initializer, "initializer");

        long index = reserveTail();
        T struct = elements().at(index);

        try
        {
            initializer.accept(struct);
            commitTail();
            return struct;
        }
        catch (RuntimeException | Error e)
        {
            struct.clear();
            throw e;
        }
    }

    public T peek()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Queue is empty");
        }

        return elements().at(headIndex());
    }

    public void dequeue()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Queue is empty");
        }

        elements().clearStruct(headIndex());
        advanceHead();
    }
}
