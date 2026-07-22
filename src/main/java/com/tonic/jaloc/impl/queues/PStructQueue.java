package com.tonic.jaloc.impl.queues;


import com.tonic.jaloc.impl.arrays.struct.*;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.*;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A growable native struct FIFO queue on a ring; elements are accessed through views, and any capacity change invalidates outstanding views.
 */
public final class PStructQueue<T extends PStruct> extends AbstractStructRing<T>
{
    /**
     * Creates an empty queue with zero capacity on the system allocator.
     *
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     */
    public PStructQueue(StructLayout layout, StructViewFactory<T> viewFactory)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, 0);
    }

    /**
     * Creates an empty queue with the given starting capacity on the system allocator.
     *
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PStructQueue(StructLayout layout, StructViewFactory<T> viewFactory, long initialCapacity)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, initialCapacity);
    }

    /**
     * Creates an empty queue with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PStructQueue(NativeAllocator allocator, StructLayout layout, StructViewFactory<T> viewFactory, long initialCapacity)
    {
        super(allocator, layout, viewFactory, initialCapacity);
    }

    /**
     * Grows capacity to at least requiredCapacity.
     *
     * @param requiredCapacity the minimum capacity
     * @throws IllegalArgumentException if requiredCapacity is negative
     * @throws IllegalStateException if closed
     */
    public void ensureCapacity(long requiredCapacity)
    {
        ensureRingCapacity(requiredCapacity);
    }

    /**
     * Shrinks capacity to the current size.
     *
     * @throws IllegalStateException if closed
     */
    public void trimToSize()
    {
        trimRing();
    }

    /**
     * Enqueues a zeroed element at the tail and returns its view.
     *
     * @return the view
     * @throws IllegalStateException if closed
     */
    public T enqueue()
    {
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
     * @throws IllegalStateException if closed
     */
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
}
