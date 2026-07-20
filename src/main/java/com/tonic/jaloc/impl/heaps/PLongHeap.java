package com.tonic.jaloc.impl.heaps;

import com.tonic.jaloc.impl.arrays.PLongArray;
import com.tonic.jaloc.impl.arrays.PLongWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveHeap;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PLongHeap extends AbstractPrimitiveHeap<PLongArray, PLongWriter> {
    public PLongHeap() {
        this(0);
    }

    public PLongHeap(long initialCapacity) {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PLongHeap(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PLongArray(allocator, initialCapacity));
    }

    @Override
    protected PLongArray createArray(NativeAllocator allocator, long capacity) {
        return new PLongArray(allocator, capacity);
    }

    public void push(long value) {
        PLongWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
        siftUp(sizeUnchecked() - 1);
    }

    public long pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        PLongArray heap = elements();
        long root = heap.getUnchecked(0);
        long last = heap.getUnchecked(sizeUnchecked() - 1);
        decrementSize();
        if (sizeUnchecked() != 0) {
            heap.setUnchecked(0, last);
            siftDown(0);
        }
        return root;
    }

    public long peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return elements().getUnchecked(0);
    }

    private void siftUp(long index) {
        PLongArray heap = elements();
        long value = heap.getUnchecked(index);
        while (index > 0) {
            long parent = (index - 1) >>> 1;
            long parentValue = heap.getUnchecked(parent);
            if (parentValue <= value) {
                break;
            }
            heap.setUnchecked(index, parentValue);
            index = parent;
        }
        heap.setUnchecked(index, value);
    }

    private void siftDown(long index) {
        PLongArray heap = elements();
        long count = sizeUnchecked();
        long value = heap.getUnchecked(index);
        while (true) {
            long child = index * 2 + 1;
            if (child >= count) {
                break;
            }
            long childValue = heap.getUnchecked(child);
            long right = child + 1;
            if (right < count) {
                long rightValue = heap.getUnchecked(right);
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
