package com.tonic.jaloc.impl.stacks;


import com.tonic.jaloc.impl.arrays.struct.*;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.*;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A growable native struct stack; elements are accessed through views.
 */
public final class PStructStack<T extends PStruct> extends AbstractNativeList<PStructArray<T>, PStructWriter<T>>
{
    private final StructLayout layout;
    private final StructViewFactory<T> viewFactory;

    /**
     * Creates an empty stack with zero capacity on the system allocator.
     *
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     */
    public PStructStack(StructLayout layout, StructViewFactory<T> viewFactory)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, 0);
    }

    /**
     * Creates an empty stack with the given starting capacity on the system allocator.
     *
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PStructStack(StructLayout layout, StructViewFactory<T> viewFactory, long initialCapacity)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, initialCapacity);
    }

    /**
     * Creates an empty stack with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PStructStack(NativeAllocator allocator, StructLayout layout, StructViewFactory<T> viewFactory, long initialCapacity)
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
     * Pushes a zeroed element and returns its view.
     *
     * @return the view
     * @throws IllegalStateException if closed
     */
    public T push()
    {
        PStructWriter<T> writer = appendWriter(1);
        T struct = writer.next();

        commitWriter();
        return struct;
    }

    /**
     * Pushes an element, initializes it, and returns its view; a throwing initializer rolls back.
     *
     * @param initializer fills the fresh element
     * @return the view
     * @throws NullPointerException if initializer is null
     * @throws IllegalStateException if closed
     */
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

    /**
     * Creates a view of the top element.
     *
     * @return the view
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public T peek()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Stack is empty");
        }

        return elements().at(size() - 1);
    }

    /**
     * Removes the top element, zeroing its slot.
     *
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
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
