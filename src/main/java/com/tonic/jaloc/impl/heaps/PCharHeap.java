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
        siftUp(size() - 1);
    }

    public char pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        PCharArray heap = elements();
        char root = heap.get(0);
        char last = heap.get(size() - 1);
        decrementSize();
        if (size() != 0) {
            heap.set(0, last);
            siftDown(0);
        }
        return root;
    }

    public char peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return elements().get(0);
    }

    private void siftUp(long index) {
        PCharArray heap = elements();
        char value = heap.get(index);
        while (index > 0) {
            long parent = (index - 1) >>> 1;
            char parentValue = heap.get(parent);
            if (parentValue <= value) {
                break;
            }
            heap.set(index, parentValue);
            index = parent;
        }
        heap.set(index, value);
    }

    private void siftDown(long index) {
        PCharArray heap = elements();
        long count = size();
        char value = heap.get(index);
        while (true) {
            long child = index * 2 + 1;
            if (child >= count) {
                break;
            }
            char childValue = heap.get(child);
            long right = child + 1;
            if (right < count) {
                char rightValue = heap.get(right);
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
