package com.tonic.jaloc.impl.fixedqueues;


import com.tonic.jaloc.impl.arrays.struct.*;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.*;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A fixed-capacity native struct FIFO queue that rejects when full.
 */
public final class PStructFixedQueue<T extends PStruct> extends AbstractStructRing<T>
{
    /**
     * Allocates a queue of the given capacity on the system allocator.
     *
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     * @param capacity the fixed capacity
     * @throws IllegalArgumentException if capacity is not positive
     */
    public PStructFixedQueue(StructLayout layout, StructViewFactory<T> viewFactory, long capacity)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, capacity);
    }

    /**
     * Allocates a queue of the given capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     * @param capacity the fixed capacity
     * @throws IllegalArgumentException if capacity is not positive
     */
    public PStructFixedQueue(NativeAllocator allocator, StructLayout layout, StructViewFactory<T> viewFactory, long capacity)
    {
        super(allocator, layout, viewFactory, requireCapacity(capacity));
    }

    /**
     * Enqueues a zeroed element at the tail and returns its view.
     *
     * @return the view
     * @throws IllegalStateException if full or closed
     */
    public T enqueue()
    {
        if (size() == capacity())
        {
            throw new IllegalStateException("Queue is full");
        }

        long index = reserveTail();
        T struct = elements().at(index);

        commitTail();
        return struct;
    }

    /**
     * Enqueues an element, initializes it, and returns its view; a throwing initializer rolls back.
     *
     * @param initializer fills the fresh element
     * @return the view
     * @throws NullPointerException if initializer is null
     * @throws IllegalStateException if full or closed
     */
    public T enqueue(Consumer<? super T> initializer)
    {
        Objects.requireNonNull(initializer, "initializer");

        if (size() == capacity())
        {
            throw new IllegalStateException("Queue is full");
        }

        return initialize(initializer);
    }

    /**
     * Enqueues and initializes an element if room remains.
     *
     * @param initializer fills the fresh element
     * @return true if accepted, false if full
     * @throws NullPointerException if initializer is null
     * @throws IllegalStateException if closed
     */
    public boolean offer(Consumer<? super T> initializer)
    {
        Objects.requireNonNull(initializer, "initializer");

        if (size() == capacity())
        {
            return false;
        }

        initialize(initializer);
        return true;
    }

    /**
     * Creates a view of the head element.
     *
     * @return the view
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public T peek()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Queue is empty");
        }

        return elements().at(headIndex());
    }

    /**
     * Removes the head element, zeroing its slot.
     *
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public void dequeue()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Queue is empty");
        }

        elements().clearStruct(headIndex());
        advanceHead();
    }

    private T initialize(Consumer<? super T> initializer)
    {
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
}
