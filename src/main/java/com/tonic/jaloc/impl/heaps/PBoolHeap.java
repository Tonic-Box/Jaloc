package com.tonic.jaloc.impl.heaps;

import com.tonic.jaloc.impl.arrays.PBoolArray;
import com.tonic.jaloc.impl.arrays.PBoolWriter;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveHeap;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.NoSuchElementException;

public final class PBoolHeap extends AbstractPrimitiveHeap<PBoolArray, PBoolWriter>
{
    public PBoolHeap()
    {
        this(0);
    }

    public PBoolHeap(long initialCapacity)
    {
        this(SystemAllocator.getInstance(), initialCapacity);
    }

    public PBoolHeap(NativeAllocator allocator, long initialCapacity) {
        super(allocator, new PBoolArray(allocator, initialCapacity));
    }

    @Override
    protected PBoolArray createArray(NativeAllocator allocator, long capacity) {
        return new PBoolArray(allocator, capacity);
    }

    public void push(boolean value)
    {
        PBoolWriter writer = appendWriter(1);
        writer.put(value);
        commitWriter();
        siftUp(size() - 1);
    }

    public boolean pop()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        PBoolArray heap = elements();
        boolean root = heap.getUnchecked(0);
        long lastIndex = size() - 1;
        boolean last = heap.getUnchecked(lastIndex);
        heap.setUnchecked(lastIndex, false);
        decrementSize();
        if (size() != 0) {
            heap.setUnchecked(0, last);
            siftDown(0);
        }
        return root;
    }

    public boolean peek()
    {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return elements().getUnchecked(0);
    }

    private void siftUp(long index)
    {
        PBoolArray heap = elements();
        boolean value = heap.getUnchecked(index);
        while (index > 0) {
            long parent = (index - 1) >>> 1;
            boolean parentValue = heap.getUnchecked(parent);
            if (Boolean.compare(parentValue, value) <= 0) {
                break;
            }
            heap.setUnchecked(index, parentValue);
            index = parent;
        }
        heap.setUnchecked(index, value);
    }

    private void siftDown(long index)
    {
        PBoolArray heap = elements();
        long count = size();
        boolean value = heap.getUnchecked(index);
        while (true) {
            long child = index * 2 + 1;
            if (child >= count) {
                break;
            }
            boolean childValue = heap.getUnchecked(child);
            long right = child + 1;
            if (right < count) {
                boolean rightValue = heap.getUnchecked(right);
                if (Boolean.compare(rightValue, childValue) < 0) {
                    child = right;
                    childValue = rightValue;
                }
            }
            if (Boolean.compare(value, childValue) <= 0) {
                break;
            }
            heap.setUnchecked(index, childValue);
            index = child;
        }
        heap.setUnchecked(index, value);
    }
}
