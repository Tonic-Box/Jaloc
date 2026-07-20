package com.tonic.jaloc.impl.stacks;


import com.tonic.jaloc.impl.arrays.struct.*;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.*;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public final class PStructStack<T extends PStruct> extends AbstractNativeList<PStructArray<T>, PStructWriter<T>>
{
    private final StructLayout layout;
    private final StructViewFactory<T> viewFactory;

    public PStructStack(StructLayout layout, StructViewFactory<T> viewFactory)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, 0);
    }

    public PStructStack(StructLayout layout, StructViewFactory<T> viewFactory, long initialCapacity)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, initialCapacity);
    }

    public PStructStack(NativeAllocator allocator, StructLayout layout, StructViewFactory<T> viewFactory, long initialCapacity)
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

    public T push()
    {
        PStructWriter<T> writer = appendWriter(1);
        T struct = writer.next();

        commitWriter();
        return struct;
    }

    public T push(Consumer<? super T> initializer)
    {
        Objects.requireNonNull(initializer, "initializer");

        PStructWriter<T> writer = appendWriter(1);
        T struct = writer.next();

        try
        {
            initializer.accept(struct);
            commitWriter();
            return struct;
        }
        catch (RuntimeException | Error e)
        {
            struct.clear();
            writer.position(size());
            throw e;
        }
    }

    public T peek()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Stack is empty");
        }

        return elements().at(size() - 1);
    }

    public void pop()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Stack is empty");
        }

        long lastIndex = size() - 1;

        elements().clearStruct(lastIndex);
        decrementSize();
    }
}
