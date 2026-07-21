package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

import java.util.Objects;

/**
 * A native array of fixed-width elements with checked and unchecked access.
 */
public abstract class AbstractPrimitiveArray<W extends AbstractArrayWriter> extends AbstractNativeArray<W>
{
    private final ElementSize elementSize;

    protected AbstractPrimitiveArray(ElementSize elementSize, long length)
    {
        this(SystemAllocator.getInstance(), elementSize, length);
    }

    protected AbstractPrimitiveArray(NativeAllocator allocator, ElementSize elementSize, long length)
    {
        this(allocator, elementSize, length, requireElementSize(elementSize).byteSize(length));
    }

    protected AbstractPrimitiveArray(NativeAllocator allocator, ElementSize elementSize, long length, long byteSize)
    {
        super(allocator, length, byteSize, requireElementSize(elementSize).getAlignment());

        this.elementSize = elementSize;
    }

    /**
     * @return the element width
     */
    public final ElementSize elementSize()
    {
        return elementSize;
    }

    @Override
    public void clearRange(long fromIndex, long toIndex)
    {
        checkRange(fromIndex, toIndex);

        long elementCount = toIndex - fromIndex;

        if (elementCount == 0)
        {
            return;
        }

        UnsafeMemory.clear(baseAddress() + fromIndex * elementSize.getSize(), elementCount * elementSize.getSize());
    }

    @Override
    protected long byteSize(long elementCount)
    {
        return elementSize.byteSize(elementCount);
    }

    protected final long offsetOf(long index)
    {
        checkIndex(index);
        return index * elementSize.getSize();
    }

    protected final byte readByte(long index)
    {
        return UnsafeMemory.getByte(baseAddress() + offsetOf(index));
    }

    protected final void writeByte(long index, byte value)
    {
        UnsafeMemory.putByte(baseAddress() + offsetOf(index), value);
    }

    protected final boolean readBoolean(long index)
    {
        return UnsafeMemory.getByte(baseAddress() + offsetOf(index)) != 0;
    }

    protected final void writeBoolean(long index, boolean value)
    {
        UnsafeMemory.putByte(baseAddress() + offsetOf(index), value ? (byte) 1 : (byte) 0);
    }

    protected final short readShort(long index)
    {
        return UnsafeMemory.getShort(baseAddress() + offsetOf(index));
    }

    protected final void writeShort(long index, short value)
    {
        UnsafeMemory.putShort(baseAddress() + offsetOf(index), value);
    }

    protected final char readChar(long index)
    {
        return UnsafeMemory.getChar(baseAddress() + offsetOf(index));
    }

    protected final void writeChar(long index, char value)
    {
        UnsafeMemory.putChar(baseAddress() + offsetOf(index), value);
    }

    protected final int readInt(long index)
    {
        return UnsafeMemory.getInt(baseAddress() + offsetOf(index));
    }

    protected final void writeInt(long index, int value)
    {
        UnsafeMemory.putInt(baseAddress() + offsetOf(index), value);
    }

    protected final long readLong(long index)
    {
        return UnsafeMemory.getLong(baseAddress() + offsetOf(index));
    }

    protected final void writeLong(long index, long value)
    {
        UnsafeMemory.putLong(baseAddress() + offsetOf(index), value);
    }

    protected final float readFloat(long index)
    {
        return UnsafeMemory.getFloat(baseAddress() + offsetOf(index));
    }

    protected final void writeFloat(long index, float value)
    {
        UnsafeMemory.putFloat(baseAddress() + offsetOf(index), value);
    }

    protected final double readDouble(long index)
    {
        return UnsafeMemory.getDouble(baseAddress() + offsetOf(index));
    }

    protected final void writeDouble(long index, double value)
    {
        UnsafeMemory.putDouble(baseAddress() + offsetOf(index), value);
    }

    protected final byte readByteUnchecked(long index)
    {
        return UnsafeMemory.getByte(baseAddress() + index * elementSize.getSize());
    }

    protected final void writeByteUnchecked(long index, byte value)
    {
        UnsafeMemory.putByte(baseAddress() + index * elementSize.getSize(), value);
    }

    protected final short readShortUnchecked(long index)
    {
        return UnsafeMemory.getShort(baseAddress() + index * elementSize.getSize());
    }

    protected final void writeShortUnchecked(long index, short value)
    {
        UnsafeMemory.putShort(baseAddress() + index * elementSize.getSize(), value);
    }

    protected final char readCharUnchecked(long index)
    {
        return UnsafeMemory.getChar(baseAddress() + index * elementSize.getSize());
    }

    protected final void writeCharUnchecked(long index, char value)
    {
        UnsafeMemory.putChar(baseAddress() + index * elementSize.getSize(), value);
    }

    protected final int readIntUnchecked(long index)
    {
        return UnsafeMemory.getInt(baseAddress() + index * elementSize.getSize());
    }

    protected final void writeIntUnchecked(long index, int value)
    {
        UnsafeMemory.putInt(baseAddress() + index * elementSize.getSize(), value);
    }

    protected final long readLongUnchecked(long index)
    {
        return UnsafeMemory.getLong(baseAddress() + index * elementSize.getSize());
    }

    protected final void writeLongUnchecked(long index, long value)
    {
        UnsafeMemory.putLong(baseAddress() + index * elementSize.getSize(), value);
    }

    protected final float readFloatUnchecked(long index)
    {
        return UnsafeMemory.getFloat(baseAddress() + index * elementSize.getSize());
    }

    protected final void writeFloatUnchecked(long index, float value)
    {
        UnsafeMemory.putFloat(baseAddress() + index * elementSize.getSize(), value);
    }

    protected final double readDoubleUnchecked(long index)
    {
        return UnsafeMemory.getDouble(baseAddress() + index * elementSize.getSize());
    }

    protected final void writeDoubleUnchecked(long index, double value)
    {
        UnsafeMemory.putDouble(baseAddress() + index * elementSize.getSize(), value);
    }

    private static ElementSize requireElementSize(ElementSize elementSize)
    {
        return Objects.requireNonNull(elementSize, "elementSize");
    }
}
