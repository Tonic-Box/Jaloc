package com.tonic.jaloc.impl.ringbuffers;


import com.tonic.jaloc.impl.arrays.struct.*;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.*;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A fixed-capacity native struct ring buffer; enqueueing when full overwrites the oldest entry.
 */
public final class PStructRingBuffer<T extends PStruct> extends AbstractNativeRing<PStructArray<T>, PStructWriter<T>>
{
    private final StructLayout layout;
    private final StructViewFactory<T> viewFactory;

    /**
     * Allocates a buffer of the given capacity on the system allocator.
     *
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     * @param capacity the fixed capacity
     * @throws IllegalArgumentException if capacity is not positive
     */
    public PStructRingBuffer(StructLayout layout, StructViewFactory<T> viewFactory, long capacity)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, capacity);
    }

    /**
     * Allocates a buffer of the given capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     * @param capacity the fixed capacity
     * @throws IllegalArgumentException if capacity is not positive
     */
    public PStructRingBuffer(NativeAllocator allocator, StructLayout layout, StructViewFactory<T> viewFactory, long capacity)
    {
        super(Objects.requireNonNull(allocator, "allocator"), new PStructArray<>(allocator, viewFactory, layout, requireCapacity(capacity)));

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
     * Enqueues a zeroed element, overwriting the oldest when full, and returns its view.
     *
     * @return the view
     * @throws IllegalStateException if closed
     */
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

    /**
     * Enqueues an element, removing the oldest first when full, and initializes it; a throwing initializer rolls back the insert.
     *
     * @param initializer fills the fresh element
     * @return the view
     * @throws NullPointerException if initializer is null
     * @throws IllegalStateException if closed
     */
    public T enqueue(Consumer<? super T> initializer)
    {
        Objects.requireNonNull(initializer, "initializer");

        if (size() == capacity())
        {
            elements().clearStruct(headIndex());
            advanceHead();
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
            throw new NoSuchElementException("Ring buffer is empty");
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
