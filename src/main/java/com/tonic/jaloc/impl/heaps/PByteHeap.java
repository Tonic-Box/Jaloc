package com.tonic.jaloc.impl.heaps;

import com.tonic.jaloc.impl.arrays.PByteArray;
import com.tonic.jaloc.impl.arrays.PByteWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveHeap;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

/**
 * A growable native byte min-heap.
 */
public final class PByteHeap extends AbstractPrimitiveHeap<PByteArray, PByteWriter> {
    /**
     * Creates an empty heap with zero capacity on the system allocator.
     */
    public PByteHeap() {
        this(0);
    }

    /**
     * Creates an empty heap with the given starting capacity on the system allocator.
     *
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PByteHeap(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    /**
     * Creates an empty heap with the given starting capacity on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param initialCapacity the starting capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public PByteHeap(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PByteArray(allocator, initialCapacity));
    }

    @Override
    protected PByteArray createArray(NativeAllocator allocator, long capacity) {
        return new PByteArray(allocator, capacity);
    }

    /**
     * Pushes value, growing if needed.
     *
     * @param value the value to push
     * @throws IllegalStateException if closed
     */
    public void push(byte value) {
        PByteWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
        siftUp(sizeUnchecked() - 1);
    }

    /**
     * Removes and returns the smallest element.
     *
     * @return the removed element
     * @throws NoSuchElementException if empty
     * @throws IllegalStateException if closed
     */
    public byte pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        PByteArray heap = elements();
        byte root = heap.getUnchecked(0);
        byte last = heap.getUnchecked(sizeUnchecked() - 1);
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
    public byte peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return elements().getUnchecked(0);
    }

    private void siftUp(long index) {
        PByteArray heap = elements();
        byte value = heap.getUnchecked(index);
        while (index > 0) {
            long parent = (index - 1) >>> 1;
            byte parentValue = heap.getUnchecked(parent);
            if (parentValue <= value) {
                break;
            }
            heap.setUnchecked(index, parentValue);
            index = parent;
        }
        heap.setUnchecked(index, value);
    }

    private void siftDown(long index) {
        PByteArray heap = elements();
        long count = sizeUnchecked();
        byte value = heap.getUnchecked(index);
        while (true) {
            long child = index * 2 + 1;
            if (child >= count) {
                break;
            }
            byte childValue = heap.getUnchecked(child);
            long right = child + 1;
            if (right < count) {
                byte rightValue = heap.getUnchecked(right);
                if (rightValue < childValue) {
                    child = right;
                    childValue = rightValue;
                }
            }
            if (value <= childValue) {
                break;
            }
            heap.setUnchecked(index, childValue);
            index = child;
        }
        heap.setUnchecked(index, value);
    }
}
