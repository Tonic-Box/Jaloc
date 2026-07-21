package com.tonic.jaloc.impl.dequeues;


import com.tonic.jaloc.impl.arrays.struct.*;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.*;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A growable native struct deque on a ring; elements are accessed through views, and any capacity change invalidates outstanding views.
 */
public final class PStructDeque<T extends PStruct> extends AbstractNativeRing<PStructArray<T>, PStructWriter<T>>
{
    private final StructLayout layout;
    private final StructViewFactory<T> viewFactory;

    /**
     * Creates an empty deque with zero capacity on the system allocator.
     *
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     */
    public PStructDeque(StructLayout layout, StructViewFactory<T> viewFactory)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, 0);
    }

    /**
     * Creates an empty deque with the given starting capacity on the system allocator.
     *
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PStructDeque(StructLayout layout, StructViewFactory<T> viewFactory, long initialCapacity)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, initialCapacity);
    }

    /**
     * Creates an empty deque with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PStructDeque(NativeAllocator allocator, StructLayout layout, StructViewFactory<T> viewFactory, long initialCapacity)
    {
        super(Objects.requireNonNull(allocator, "allocator"), new PStructArray<>(allocator, viewFactory, layout, initialCapacity));

        this.layout = layout;
        this.viewFactory = viewFactory;
    }

    /**
     * @return the entry layout
     */
    public StructLayout layout()
    {
        return layout;
    }

    @Override
    protected PStructArray<T> createArray(NativeAllocator allocator, long capacity)
    {
        return new PStructArray<>(allocator, viewFactory, layout, capacity);
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
     * Adds a zeroed element at the head and returns its view.
     *
     * @return the view
     * @throws IllegalStateException if closed
     */
    public T addFirst()
    {
        long index = reserveHead();
        T struct = elements().at(index);

        commitHead();
        return struct;
    }

    /**
     * Adds an element at the head, initializes it, and returns its view; a throwing initializer rolls back.
     *
     * @param initializer fills the fresh element
     * @return the view
     * @throws NullPointerException if initializer is null
     * @throws IllegalStateException if closed
     */
    public T addFirst(Consumer<? super T> initializer)
    {
        Objects.requireNonNull(initializer, "initializer");

        long index = reserveHead();
        T struct = elements().at(index);

        try
        {
            initializer.accept(struct);
            commitHead();
            return struct;
        }
        catch (RuntimeException | Error e)
        {
            struct.clear();
            rotateHead();
            throw e;
        }
    }

    /**
     * Adds a zeroed element at the tail and returns its view.
     *
     * @return the view
     * @throws IllegalStateException if closed
     */
    public T addLast()
    {
        long index = reserveTail();
        T struct = elements().at(index);

        commitTail();
        return struct;
    }

    /**
     * Adds an element at the tail, initializes it, and returns its view; a throwing initializer rolls back.
     *
     * @param initializer fills the fresh element
     * @return the view
     * @throws NullPointerException if initializer is null
     * @throws IllegalStateException if closed
     */
    public T addLast(Consumer<? super T> initializer)
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
    public T peekFirst()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Deque is empty");
        }

        return elements().at(headIndex());
    }

    /**
     * Creates a view of the tail element.
     *
     * @return the view
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public T peekLast()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Deque is empty");
        }

        return elements().at(tailIndex());
    }

    /**
     * Removes the head element, zeroing its slot.
     *
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public void removeFirst()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Deque is empty");
        }

        elements().clearStruct(headIndex());
        advanceHead();
    }

    /**
     * Removes the tail element, zeroing its slot.
     *
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public void removeLast()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Deque is empty");
        }

        elements().clearStruct(tailIndex());
        shrinkTail();
    }
}
