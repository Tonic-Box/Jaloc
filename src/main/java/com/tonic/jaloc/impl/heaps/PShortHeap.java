package com.tonic.jaloc.impl.heaps;

import com.tonic.jaloc.impl.arrays.PShortArray;
import com.tonic.jaloc.impl.arrays.PShortWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveHeap;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PShortHeap extends AbstractPrimitiveHeap<PShortArray, PShortWriter> {
    public PShortHeap() {
        this(0);
    }

    public PShortHeap(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PShortHeap(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PShortArray(allocator, initialCapacity));
    }

    @Override
    protected PShortArray createArray(NativeAllocator allocator, long capacity) {
        return new PShortArray(allocator, capacity);
    }

    public void push(short value) {
        PShortWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
        siftUp(size() - 1);
    }

    public short pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        PShortArray heap = elements();
        short root = heap.getUnchecked(0);
        short last = heap.getUnchecked(size() - 1);
        decrementSize();
        if (size() != 0) {
            heap.setUnchecked(0, last);
            siftDown(0);
        }
        return root;
    }

    public short peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return elements().getUnchecked(0);
    }

    private void siftUp(long index) {
        PShortArray heap = elements();
        short value = heap.getUnchecked(index);
        while (index > 0) {
            long parent = (index - 1) >>> 1;
            short parentValue = heap.getUnchecked(parent);
            if (parentValue <= value) {
                break;
            }
            heap.setUnchecked(index, parentValue);
            index = parent;
        }
        heap.setUnchecked(index, value);
    }

    private void siftDown(long index) {
        PShortArray heap = elements();
        long count = size();
        short value = heap.getUnchecked(index);
        while (true) {
            long child = index * 2 + 1;
            if (child >= count) {
                break;
            }
            short childValue = heap.getUnchecked(child);
            long right = child + 1;
            if (right < count) {
                short rightValue = heap.getUnchecked(right);
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
