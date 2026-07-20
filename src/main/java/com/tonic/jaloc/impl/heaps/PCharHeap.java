package com.tonic.jaloc.impl.heaps;

import com.tonic.jaloc.impl.arrays.PCharArray;
import com.tonic.jaloc.impl.arrays.PCharWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveHeap;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PCharHeap extends AbstractPrimitiveHeap<PCharArray, PCharWriter> {
    public PCharHeap() {
        this(0);
    }

    public PCharHeap(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PCharHeap(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PCharArray(allocator, initialCapacity));
    }

    @Override
    protected PCharArray createArray(NativeAllocator allocator, long capacity) {
        return new PCharArray(allocator, capacity);
    }

    public void push(char value) {
        PCharWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
        siftUp(sizeUnchecked() - 1);
    }

    public char pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        PCharArray heap = elements();
        char root = heap.getUnchecked(0);
        char last = heap.getUnchecked(sizeUnchecked() - 1);
        decrementSize();
        if (sizeUnchecked() != 0) {
            heap.setUnchecked(0, last);
            siftDown(0);
        }
        return root;
    }

    public char peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return elements().getUnchecked(0);
    }

    private void siftUp(long index) {
        PCharArray heap = elements();
        char value = heap.getUnchecked(index);
        while (index > 0) {
            long parent = (index - 1) >>> 1;
            char parentValue = heap.getUnchecked(parent);
            if (parentValue <= value) {
                break;
            }
            heap.setUnchecked(index, parentValue);
            index = parent;
        }
        heap.setUnchecked(index, value);
    }

    private void siftDown(long index) {
        PCharArray heap = elements();
        long count = sizeUnchecked();
        char value = heap.getUnchecked(index);
        while (true) {
            long child = index * 2 + 1;
            if (child >= count) {
                break;
            }
            char childValue = heap.getUnchecked(child);
            long right = child + 1;
            if (right < count) {
                char rightValue = heap.getUnchecked(right);
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
