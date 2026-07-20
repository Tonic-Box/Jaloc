package com.tonic.jaloc.impl.heaps;

import com.tonic.jaloc.impl.arrays.PIntArray;
import com.tonic.jaloc.impl.arrays.PIntWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveHeap;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PIntHeap extends AbstractPrimitiveHeap<PIntArray, PIntWriter> {
    public PIntHeap() {
        this(0);
    }

    public PIntHeap(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PIntHeap(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PIntArray(allocator, initialCapacity));
    }

    @Override
    protected PIntArray createArray(NativeAllocator allocator, long capacity) {
        return new PIntArray(allocator, capacity);
    }

    public void push(int value) {
        PIntWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
        siftUp(size() - 1);
    }

    public int pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        PIntArray heap = elements();
        int root = heap.get(0);
        int last = heap.get(size() - 1);
        decrementSize();
        if (size() != 0) {
            heap.set(0, last);
            siftDown(0);
        }
        return root;
    }

    public int peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return elements().get(0);
    }

    private void siftUp(long index) {
        PIntArray heap = elements();
        int value = heap.get(index);
        while (index > 0) {
            long parent = (index - 1) >>> 1;
            int parentValue = heap.get(parent);
            if (parentValue <= value) {
                break;
            }
            heap.set(index, parentValue);
            index = parent;
        }
        heap.set(index, value);
    }

    private void siftDown(long index) {
        PIntArray heap = elements();
        long count = size();
        int value = heap.get(index);
        while (true) {
            long child = index * 2 + 1;
            if (child >= count) {
                break;
            }
            int childValue = heap.get(child);
            long right = child + 1;
            if (right < count) {
                int rightValue = heap.get(right);
                if (rightValue < childValue) {
                    child = right;
                    childValue = rightValue;
                }
            }
            if (value <= childValue) {
                break;
            }
            heap.set(index, childValue);
            index = child;
        }
        heap.set(index, value);
    }
}
