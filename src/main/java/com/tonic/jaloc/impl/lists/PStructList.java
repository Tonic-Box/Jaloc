package com.tonic.jaloc.impl.lists;


import com.tonic.jaloc.impl.arrays.struct.*;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.*;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A growable native struct list; elements are accessed through views, and any capacity change invalidates outstanding views.
 */
public final class PStructList<T extends PStruct> extends AbstractStructList<T> implements Iterable<T>
{
    /**
     * Creates an empty list with zero capacity on the system allocator.
     *
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     */
    public PStructList(StructLayout layout, StructViewFactory<T> viewFactory)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, 0);
    }

    /**
     * Creates an empty list with the given starting capacity on the system allocator.
     *
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PStructList(StructLayout layout, StructViewFactory<T> viewFactory, long initialCapacity)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, initialCapacity);
    }

    /**
     * Creates an empty list with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PStructList(NativeAllocator allocator, StructLayout layout, StructViewFactory<T> viewFactory, long initialCapacity)
    {
        super(allocator, layout, viewFactory, initialCapacity);
    }

    /**
     * Appends a zeroed element and returns its view.
     *
     * @return the view
     * @throws IllegalStateException if closed
     */
    public T add()
    {
        PStructWriter<T> writer = appendWriter(1);
        T struct = writer.next();

        commitWriter();
        return struct;
    }

    /**
     * Appends an element, initializes it, and returns its view; a throwing initializer rolls back.
     *
     * @param initializer fills the fresh element
     * @return the view
     * @throws NullPointerException if initializer is null
     * @throws IllegalStateException if closed
     */
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

    /**
     * Creates a view of the element at index.
     *
     * @param index the element index
     * @return a fresh view
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException if closed
     */
    public T get(long index)
    {
        checkElementIndex(index);
        return elements().at(index);
    }

    /**
     * Removes the last element, zeroing its slot.
     *
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
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

    /**
     * Sorts the live range by comparator.
     *
     * @param comparator the ordering
     * @throws NullPointerException if comparator is null
     * @throws IllegalStateException if closed
     */
    public void sort(Comparator<? super T> comparator)
    {
        elements().sort(0, size(), comparator);
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

    /**
     * Iterates through one reusable view; do not hold it across next() calls.
     *
     * @return the iterator
     * @throws IllegalStateException if closed
     */
    public Iterator<T> cursorIterator()
    {
        ensureOpen();

        final long fence = size();
        final T cursor = elements().cursor();

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

                cursor.moveTo(index++);
                return cursor;
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }
}
