package com.tonic.jaloc.impl.arrays;

import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractPrimitiveArray;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

/**
 * A fixed-length native bool array, bit-packed eight per byte.
 */
public final class PBoolArray extends AbstractPrimitiveArray<PBoolWriter>
{
    /**
     * Allocates length elements on the system allocator, zeroed.
     *
     * @param length the element count
     * @throws IllegalArgumentException if length is negative
     */
    public PBoolArray(long length)
    {
        this(SystemAllocator.getInstance(), length);
    }

    /**
     * Allocates length elements on the given allocator, zeroed.
     *
     * @param allocator the allocator to source memory from
     * @param length the element count
     * @throws IllegalArgumentException if length is negative
     */
    public PBoolArray(NativeAllocator allocator, long length)
    {
        super(allocator, ElementSize.BYTE, length, packedByteSize(length));
    }


    private static int bitMask(long index)
    {
        return 1 << (int) (index & 7L);
    }

    private static long byteOffset(long index)
    {
        return index >>> 3;
    }

    private static long packedByteSize(long length)
    {
        if(length < 0)
        {
            throw new IllegalArgumentException("length cannot be negative");
        }

        return (length >>> 3) + ((length & 7L) == 0 ? 0 : 1);
    }

    /**
     * Reads the element at index.
     *
     * @param index the element index
     * @return the element
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException if closed
     */
    public boolean get(long index)
    {
        checkIndex(index);
        return getUnchecked(index);
    }

    /**
     * Writes the element at index.
     *
     * @param index the element index
     * @param value the value to store
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws IllegalStateException if closed
     */
    public void set(long index, boolean value)
    {
        checkIndex(index);
        setUnchecked(index, value);
    }

    /**
     * Reads the element at index with no liveness or bounds check; the caller must have proven both.
     *
     * @param index the element index
     * @return the element
     */
    public boolean getUnchecked(long index)
    {
        byte packed = UnsafeMemory.getByte(baseAddress() + byteOffset(index));
        return (packed & bitMask(index)) != 0;
    }

    /**
     * Writes the element at index with no liveness or bounds check; the caller must have proven both.
     *
     * @param index the element index
     * @param value the value to store
     */
    public void setUnchecked(long index, boolean value)
    {
        long address = baseAddress() + byteOffset(index);
        int mask = bitMask(index);
        byte packed = UnsafeMemory.getByte(address);
        packed = value ? (byte) (packed | mask) : (byte) (packed & ~mask);
        UnsafeMemory.putByte(address, packed);
    }

    @Override
    public PBoolWriter writer()
    {
        return new PBoolWriter(this);
    }

    @Override
    protected long byteSize(long elementCount)
    {
        return packedByteSize(elementCount);
    }

    @Override
    public void clearRange(long fromIndex, long toIndex)
    {
        checkRange(fromIndex, toIndex);

        if (fromIndex == toIndex)
        {
            return;
        }

        long base = baseAddress();
        long fromByte = fromIndex >>> 3;
        long toByte = toIndex >>> 3;

        if (fromByte == toByte)
        {
            int keep = ((1 << (int) (fromIndex & 7L)) - 1) | ~((1 << (int) (toIndex & 7L)) - 1);
            byte packed = UnsafeMemory.getByte(base + fromByte);
            UnsafeMemory.putByte(base + fromByte, (byte) (packed & keep));
            return;
        }

        if ((fromIndex & 7L) != 0)
        {
            int keep = (1 << (int) (fromIndex & 7L)) - 1;
            byte packed = UnsafeMemory.getByte(base + fromByte);
            UnsafeMemory.putByte(base + fromByte, (byte) (packed & keep));
            fromByte++;
        }

        if (toByte > fromByte)
        {
            UnsafeMemory.clear(base + fromByte, toByte - fromByte);
        }

        if ((toIndex & 7L) != 0)
        {
            int keep = ~((1 << (int) (toIndex & 7L)) - 1);
            byte packed = UnsafeMemory.getByte(base + toByte);
            UnsafeMemory.putByte(base + toByte, (byte) (packed & keep));
        }
    }
}
