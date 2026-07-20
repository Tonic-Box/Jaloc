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
        byte root = heap.get(0);
        byte last = heap.get(size() - 1);
        decrementSize();
        if (size() != 0) {
            heap.set(0, last);
            siftDown(0);
        }
        return root;
    }

    public byte peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return elements().get(0);
    }

    private void siftUp(long index) {
        PByteArray heap = elements();
        byte value = heap.get(index);
        while (index > 0) {
            long parent = (index - 1) >>> 1;
            byte parentValue = heap.get(parent);
            if (parentValue <= value) {
                break;
            }
            heap.set(index, parentValue);
            index = parent;
        }
        heap.set(index, value);
    }

    private void siftDown(long index) {
        PByteArray heap = elements();
        long count = size();
        byte value = heap.get(index);
        while (true) {
            long child = index * 2 + 1;
            if (child >= count) {
                break;
            }
            byte childValue = heap.get(child);
            long right = child + 1;
            if (right < count) {
                byte rightValue = heap.get(right);
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
