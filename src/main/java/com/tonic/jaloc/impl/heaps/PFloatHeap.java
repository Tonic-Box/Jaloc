package com.tonic.jaloc.impl.heaps;

import com.tonic.jaloc.impl.arrays.PFloatArray;
import com.tonic.jaloc.impl.arrays.PFloatWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveHeap;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PFloatHeap extends AbstractPrimitiveHeap<PFloatArray, PFloatWriter> {
    public PFloatHeap() {
        this(0);
    }

    public PFloatHeap(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PFloatHeap(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PFloatArray(allocator, initialCapacity));
    }

    @Override
    protected PFloatArray createArray(NativeAllocator allocator, long capacity) {
        return new PFloatArray(allocator, capacity);
    }

    public void push(float value) {
        PFloatWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
        siftUp(size() - 1);
    }

    public float pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        PFloatArray heap = elements();
        float root = heap.get(0);
        float last = heap.get(size() - 1);
        decrementSize();
        if (size() != 0) {
            heap.set(0, last);
            siftDown(0);
        }
        return root;
    }

    public float peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return elements().get(0);
    }

    private void siftUp(long index) {
        PFloatArray heap = elements();
        float value = heap.get(index);
        while (index > 0) {
            long parent = (index - 1) >>> 1;
            float parentValue = heap.get(parent);
            if (Float.compare(parentValue, value) <= 0) {
                break;
            }
            heap.set(index, parentValue);
            index = parent;
        }
        heap.set(index, value);
    }

    private void siftDown(long index) {
        PFloatArray heap = elements();
        long count = size();
        float value = heap.get(index);
        while (true) {
            long child = index * 2 + 1;
            if (child >= count) {
                break;
            }
            float childValue = heap.get(child);
            long right = child + 1;
            if (right < count) {
                float rightValue = heap.get(right);
                if (Float.compare(rightValue, childValue) < 0) {
                    child = right;
                    childValue = rightValue;
                }
            }
            if (Float.compare(value, childValue) <= 0) {
                break;
            }
            heap.set(index, childValue);
            index = child;
        }
        heap.set(index, value);
    }
}
