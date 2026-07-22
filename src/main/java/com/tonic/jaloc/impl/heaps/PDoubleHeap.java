package com.tonic.jaloc.impl.heaps;

import com.tonic.jaloc.impl.arrays.PDoubleArray;
import com.tonic.jaloc.impl.arrays.PDoubleWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveHeap;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A growable native double min-heap.
 */
public final class PDoubleHeap extends AbstractPrimitiveHeap<PDoubleArray, PDoubleWriter> {
    /**
     * Creates an empty heap with zero capacity on the system allocator.
     */
    public PDoubleHeap() {
        this(0);
    }

    /**
     * Creates an empty heap with the given starting capacity on the system allocator.
     *
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PDoubleHeap(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    /**
     * Creates an empty heap with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PDoubleHeap(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PDoubleArray(allocator, initialCapacity));
    }

    /**
     * Builds a heap from values in one heapify pass on the system allocator.
     *
     * @param values the values to heapify
     * @throws NullPointerException if values is null
     */
    public PDoubleHeap(double... values) {
        this(SystemAllocator.getInstance(), values);
    }

    /**
     * Builds a heap from values in one heapify pass on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param values the values to heapify
     * @throws NullPointerException if values is null
     */
    public PDoubleHeap(NativeAllocator allocator, double... values) {
        super(allocator, new PDoubleArray(allocator, Objects.requireNonNull(values, "values").length));
        elementsUnchecked().copyFrom(values, 0, 0, values.length);
        size(values.length);
        heapify();
    }

    @Override
    protected PDoubleArray createArray(NativeAllocator allocator, long capacity) {
        return new PDoubleArray(allocator, capacity);
    }

    /**
     * Pushes value, growing if needed.
     *
     * @param value the value to push
     * @throws IllegalStateException if closed
     */
    public void push(double value) {
        long s = appendIndex();
        UnsafeMemory.putDouble(elementsBase() + (s << 3), value);
        size(s + 1);
        siftUp(s);
    }

    /**
     * Removes and returns the smallest element.
     *
     * @return the removed element
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public double pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        PDoubleArray heap = elements();
        double root = heap.getUnchecked(0);
        double last = heap.getUnchecked(sizeUnchecked() - 1);
        decrementSize();
        if (sizeUnchecked() != 0) {
            heap.setUnchecked(0, last);
            siftDown(0);
        }
        return root;
    }

    /**
     * Reads the smallest element without removing it.
     *
     * @return the smallest element
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public double peek() {
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
        PDoubleArray heap = elements();
        double value = heap.getUnchecked(index);
        while (index > 0) {
            long parent = (index - 1) >>> 2;
            double parentValue = heap.getUnchecked(parent);
            if (Double.compare(parentValue, value) <= 0) {
                break;
            }
            heap.setUnchecked(index, parentValue);
            index = parent;
        }
        heap.setUnchecked(index, value);
    }

    private void siftDown(long index) {
        PDoubleArray heap = elements();
        long count = sizeUnchecked();
        double value = heap.getUnchecked(index);
        while (true) {
            long child = (index << 2) + 1;
            if (child >= count) {
                break;
            }
            double childValue = heap.getUnchecked(child);
            long limit = Math.min(child + 4, count);
            for (long next = child + 1; next < limit; next++) {
                double nextValue = heap.getUnchecked(next);
                if (Double.compare(nextValue, childValue) < 0) {
                    child = next;
                    childValue = nextValue;
                }
            }
            if (Double.compare(value, childValue) <= 0) {
                break;
            }
            heap.setUnchecked(index, childValue);
            index = child;
        }
        heap.setUnchecked(index, value);
    }
}
