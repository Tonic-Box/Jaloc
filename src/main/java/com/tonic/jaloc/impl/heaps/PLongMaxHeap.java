package com.tonic.jaloc.impl.heaps;

import com.tonic.jaloc.impl.arrays.PLongArray;
import com.tonic.jaloc.impl.arrays.PLongWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveHeap;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A growable native long max-heap.
 */
public final class PLongMaxHeap extends AbstractPrimitiveHeap<PLongArray, PLongWriter> {
    /**
     * Creates an empty heap with zero capacity on the system allocator.
     */
    public PLongMaxHeap() {
        this(0);
    }

    /**
     * Creates an empty heap with the given starting capacity on the system allocator.
     *
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PLongMaxHeap(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    /**
     * Creates an empty heap with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PLongMaxHeap(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PLongArray(allocator, initialCapacity));
    }

    /**
     * Builds a heap from values in one heapify pass on the system allocator.
     *
     * @param values the values to heapify
     * @throws NullPointerException if values is null
     */
    public PLongMaxHeap(long... values) {
        this(SystemAllocator.getInstance(), values);
    }

    /**
     * Builds a heap from values in one heapify pass on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param values the values to heapify
     * @throws NullPointerException if values is null
     */
    public PLongMaxHeap(NativeAllocator allocator, long... values) {
        super(allocator, new PLongArray(allocator, Objects.requireNonNull(values, "values").length));
        elementsUnchecked().copyFrom(values, 0, 0, values.length);
        size(values.length);
        heapify();
    }

    @Override
    protected PLongArray createArray(NativeAllocator allocator, long capacity) {
        return new PLongArray(allocator, capacity);
    }

    /**
     * Pushes value, growing if needed.
     *
     * @param value the value to push
     * @throws IllegalStateException if closed
     */
    public void push(long value) {
        long s = appendIndex();
        UnsafeMemory.putLong(elementsBase() + (s << 3), value);
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
    public long pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        PLongArray heap = elements();
        long root = heap.getUnchecked(0);
        long last = heap.getUnchecked(sizeUnchecked() - 1);
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
    public long peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return elements().getUnchecked(0);
    }

    private void heapify() {
        for (long index = (sizeUnchecked() - 2) >> 2; index >= 0; index--) {
            siftDown(index);
        }
    }

    private void siftUp(long index) {
        PLongArray heap = elements();
        long value = heap.getUnchecked(index);
        while (index > 0) {
            long parent = (index - 1) >>> 2;
            long parentValue = heap.getUnchecked(parent);
            if (parentValue >= value) {
                break;
            }
            heap.setUnchecked(index, parentValue);
            index = parent;
        }
        heap.setUnchecked(index, value);
    }

    private void siftDown(long index) {
        PLongArray heap = elements();
        long count = sizeUnchecked();
        long value = heap.getUnchecked(index);
        while (true) {
            long child = (index << 2) + 1;
            if (child >= count) {
                break;
            }
            long childValue = heap.getUnchecked(child);
            long limit = Math.min(child + 4, count);
            for (long next = child + 1; next < limit; next++) {
                long nextValue = heap.getUnchecked(next);
                if (nextValue > childValue) {
                    child = next;
                    childValue = nextValue;
                }
            }
            if (value >= childValue) {
                break;
            }
            heap.setUnchecked(index, childValue);
            index = child;
        }
        heap.setUnchecked(index, value);
    }
}
