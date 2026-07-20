package com.tonic.jaloc.impl.buffers;


import com.tonic.jaloc.impl.arrays.struct.*;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.*;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public final class PStructRingBuffer<T extends PStruct> extends AbstractNativeRing<PStructArray<T>, PStructWriter<T>>
{
    private final StructLayout layout;
    private final StructViewFactory<T> viewFactory;

    public PStructRingBuffer(StructLayout layout, StructViewFactory<T> viewFactory, long capacity)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, capacity);
    }

    public PStructRingBuffer(NativeAllocator allocator, StructLayout layout, StructViewFactory<T> viewFactory, long capacity)
    {
        super(Objects.requireNonNull(allocator, "allocator"), new PStructArray<>(allocator, viewFactory, layout, requireCapacity(capacity)));

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

    public T enqueue()
    {
        if (size() == capacity())
        {
            long index = headIndex();

            elements().clearStruct(index);
            rotateHead();

            return elements().at(index);
        }

        long index = reserveTail();
        T struct = elements().at(index);

        commitTail();
        return struct;
    }

    public T enqueue(Consumer<? super T> initializer)
    {
        Objects.requireNonNull(initializer, "initializer");

        if (size() == capacity())
        {
            long index = headIndex();

            elements().clearStruct(index);
            rotateHead();

            T struct = elements().at(index);

            try
            {
                initializer.accept(struct);
                return struct;
            }
            catch (RuntimeException | Error e)
            {
                struct.clear();
                throw e;
            }
        }

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
            throw new NoSuchElementException("Ring buffer is empty");
        }

        return elements().at(headIndex());
    }

    public void dequeue()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Ring buffer is empty");
        }

        elements().clearStruct(headIndex());
        advanceHead();
    }

    private static long requireCapacity(long capacity)
    {
        if (capacity <= 0)
        {
            throw new IllegalArgumentException("capacity must be positive");
        }

        return capacity;
    }
}
