package com.tonic.jaloc.impl.heaps;

import com.tonic.jaloc.impl.arrays.PBoolArray;
import com.tonic.jaloc.impl.arrays.PBoolWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveHeap;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A growable native bool max-heap.
 */
public final class PBoolMaxHeap extends AbstractPrimitiveHeap<PBoolArray, PBoolWriter>
{
    /**
     * Creates an empty heap with zero capacity on the system allocator.
     */
    public PBoolMaxHeap()
    {
        this(0);
    }

    /**
     * Creates an empty heap with the given starting capacity on the system allocator.
     *
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PBoolMaxHeap(long initialCapacity)
    {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    /**
     * Creates an empty heap with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PBoolMaxHeap(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PBoolArray(allocator, initialCapacity));
    }

    /**
     * Builds a heap from values in one heapify pass on the system allocator.
     *
     * @param values the values to heapify
     * @throws NullPointerException if values is null
     */
    public PBoolMaxHeap(boolean... values)
    {
        this(SystemAllocator.getInstance(), values);
    }

    /**
     * Builds a heap from values in one heapify pass on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param values the values to heapify
     * @throws NullPointerException if values is null
     */
    public PBoolMaxHeap(NativeAllocator allocator, boolean... values)
    {
        super(allocator, new PBoolArray(allocator, Objects.requireNonNull(values, "values").length));
        for (int i = 0; i < values.length; i++) {
            elementsUnchecked().setUnchecked(i, values[i]);
        }
        size(values.length);
        heapify();
    }

    @Override
    protected PBoolArray createArray(NativeAllocator allocator, long capacity) {
        return new PBoolArray(allocator, capacity);
    }

    /**
     * Pushes value, growing if needed.
     *
     * @param value the value to push
     * @throws IllegalStateException if closed
     */
    public void push(boolean value)
    {
        long s = appendIndex();
        elementsUnchecked().setUnchecked(s, value);
        size(s + 1);
        siftUp(s);
    }

    /**
     * Removes and returns the largest element.
     *
     * @return the removed element
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public boolean pop()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        PBoolArray heap = elements();
        boolean root = heap.getUnchecked(0);
        long lastIndex = sizeUnchecked() - 1;
        boolean last = heap.getUnchecked(lastIndex);
        heap.setUnchecked(lastIndex, false);
        decrementSize();
        if (sizeUnchecked() != 0) {
            heap.setUnchecked(0, last);
            siftDown(0);
        }
        return root;
    }

    /**
     * Reads the largest element without removing it.
     *
     * @return the largest element
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public boolean peek()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return elements().getUnchecked(0);
    }

    private void heapify()
    {
        for (long index = (sizeUnchecked() - 2) >> 2; index >= 0; index--) {
            siftDown(index);
        }
    }

    private void siftUp(long index)
    {
        PBoolArray heap = elements();
        boolean value = heap.getUnchecked(index);
        while (index > 0) {
            long parent = (index - 1) >>> 2;
            boolean parentValue = heap.getUnchecked(parent);
            if (Boolean.compare(parentValue, value) >= 0) {
                break;
            }
            heap.setUnchecked(index, parentValue);
            index = parent;
        }
        heap.setUnchecked(index, value);
    }

    private void siftDown(long index)
    {
        PBoolArray heap = elements();
        long count = sizeUnchecked();
        boolean value = heap.getUnchecked(index);
        while (true) {
            long child = (index << 2) + 1;
            if (child >= count) {
                break;
            }
            boolean childValue = heap.getUnchecked(child);
            long limit = Math.min(child + 4, count);
            for (long next = child + 1; next < limit; next++) {
                boolean nextValue = heap.getUnchecked(next);
                if (Boolean.compare(nextValue, childValue) > 0) {
                    child = next;
                    childValue = nextValue;
                }
            }
            if (Boolean.compare(value, childValue) >= 0) {
                break;
            }
            heap.setUnchecked(index, childValue);
            index = child;
        }
        heap.setUnchecked(index, value);
    }
}
