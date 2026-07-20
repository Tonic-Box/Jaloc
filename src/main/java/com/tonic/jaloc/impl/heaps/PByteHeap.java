package com.tonic.jaloc.impl.heaps;

import com.tonic.jaloc.impl.arrays.PByteArray;
import com.tonic.jaloc.impl.arrays.PByteWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveHeap;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PByteHeap extends AbstractPrimitiveHeap<PByteArray, PByteWriter> {
    public PByteHeap() {
        this(0);
    }

    public PByteHeap(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PByteHeap(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PByteArray(allocator, initialCapacity));
    }

    @Override
    protected PByteArray createArray(NativeAllocator allocator, long capacity) {
        return new PByteArray(allocator, capacity);
    }

    public void push(byte value) {
        PByteWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
        siftUp(size() - 1);
    }

    public byte pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        PByteArray heap = elements();
        byte root = heap.getUnchecked(0);
        byte last = heap.getUnchecked(size() - 1);
        decrementSize();
        if (size() != 0) {
            heap.setUnchecked(0, last);
            siftDown(0);
        }
        return root;
    }

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
        long count = size();
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
