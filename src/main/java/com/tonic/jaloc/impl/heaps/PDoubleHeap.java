package com.tonic.jaloc.impl.heaps;

import com.tonic.jaloc.impl.arrays.PDoubleArray;
import com.tonic.jaloc.impl.arrays.PDoubleWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveHeap;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PDoubleHeap extends AbstractPrimitiveHeap<PDoubleArray, PDoubleWriter> {
    public PDoubleHeap() {
        this(0);
    }

    public PDoubleHeap(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PDoubleHeap(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PDoubleArray(allocator, initialCapacity));
    }

    @Override
    protected PDoubleArray createArray(NativeAllocator allocator, long capacity) {
        return new PDoubleArray(allocator, capacity);
    }

    public void push(double value) {
        PDoubleWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
        siftUp(size() - 1);
    }

    public double pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        PDoubleArray heap = elements();
        double root = heap.get(0);
        double last = heap.get(size() - 1);
        decrementSize();
        if (size() != 0) {
            heap.set(0, last);
            siftDown(0);
        }
        return root;
    }

    public double peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return elements().get(0);
    }

    private void siftUp(long index) {
        PDoubleArray heap = elements();
        double value = heap.get(index);
        while (index > 0) {
            long parent = (index - 1) >>> 1;
            double parentValue = heap.get(parent);
            if (Double.compare(parentValue, value) <= 0) {
                break;
            }
            heap.set(index, parentValue);
            index = parent;
        }
        heap.set(index, value);
    }

    private void siftDown(long index) {
        PDoubleArray heap = elements();
        long count = size();
        double value = heap.get(index);
        while (true) {
            long child = index * 2 + 1;
            if (child >= count) {
                break;
            }
            double childValue = heap.get(child);
            long right = child + 1;
            if (right < count) {
                double rightValue = heap.get(right);
                if (Double.compare(rightValue, childValue) < 0) {
                    child = right;
                    childValue = rightValue;
                }
            }
            if (Double.compare(value, childValue) <= 0) {
                break;
            }
            heap.set(index, childValue);
            index = child;
        }
        heap.set(index, value);
    }
}
