package com.tonic.jaloc.impl.lists;


import com.tonic.jaloc.impl.arrays.struct.*;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.*;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public final class PStructList<T extends PStruct> extends AbstractNativeList<PStructArray<T>, PStructWriter<T>> implements Iterable<T>
{
    private final StructLayout layout;
    private final StructViewFactory<T> viewFactory;

    public PStructList(StructLayout layout, StructViewFactory<T> viewFactory)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, 0);
    }

    public PStructList(StructLayout layout, StructViewFactory<T> viewFactory, long initialCapacity)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, initialCapacity);
    }

    public PStructList(NativeAllocator allocator, StructLayout layout, StructViewFactory<T> viewFactory, long initialCapacity)
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

    @Override
    protected void copyElements(PStructArray<T> source, PStructArray<T> destination, long elementCount)
    {
        for (long i = 0; i < elementCount; i++)
        {
            source.copyStructTo(i, destination, i);
        }
    }

    public T add()
    {
        PStructWriter<T> writer = appendWriter(1);
        T struct = writer.next();

        commitWriter();
        return struct;
    }

    public T add(Consumer<? super T> initializer)
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

    public T get(long index)
    {
        checkElementIndex(index);
        return elements().at(index);
    }

    public void removeLast()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("List is empty");
        }

        long lastIndex = size() - 1;

        elements().clearStruct(lastIndex);
        decrementSize();
    }

    @Override
    @NotNull
    public Iterator<T> iterator()
    {
        ensureOpen();

        final long fence = size();

        return new Iterator<T>()
        {
            private long index;

            @Override
            public boolean hasNext()
            {
                return index < fence;
            }

            @Override
            public T next()
            {
                if (!hasNext())
                {
                    throw new NoSuchElementException();
                }

                return get(index++);
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }
}