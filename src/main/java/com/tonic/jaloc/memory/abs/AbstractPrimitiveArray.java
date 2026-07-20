package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.data.ElementSize;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.Objects;

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

        long byteOffset = elementSize.byteSize(fromIndex);
        long bytes = elementSize.byteSize(elementCount);

        memory().slice(byteOffset, bytes).clear();
    }

    protected final long offsetOf(long index)
    {
        checkIndex(index);
        return elementSize.offsetOf(index);
    }

    protected final byte readByte(long index)
    {
        return memory().getByte(offsetOf(index));
    }

    protected final void writeByte(long index, byte value)
    {
        memory().putByte(offsetOf(index), value);
    }

    protected final boolean readBoolean(long index)
    {
        return memory().getByte(offsetOf(index)) != 0;
    }

    protected final void writeBoolean(long index, boolean value)
    {
        memory().putByte(offsetOf(index), value ? (byte) 1 : (byte) 0);
    }

    protected final short readShort(long index)
    {
        return memory().getShort(offsetOf(index));
    }

    protected final void writeShort(long index, short value)
    {
        memory().putShort(offsetOf(index), value);
    }

    protected final char readChar(long index)
    {
        return memory().getChar(offsetOf(index));
    }

    protected final void writeChar(long index, char value)
    {
        memory().putChar(offsetOf(index), value);
    }

    protected final int readInt(long index)
    {
        return memory().getInt(offsetOf(index));
    }

    protected final void writeInt(long index, int value)
    {
        memory().putInt(offsetOf(index), value);
    }

    protected final long readLong(long index)
    {
        return memory().getLong(offsetOf(index));
    }

    protected final void writeLong(long index, long value)
    {
        memory().putLong(offsetOf(index), value);
    }

    protected final float readFloat(long index)
    {
        return memory().getFloat(offsetOf(index));
    }

    protected final void writeFloat(long index, float value)
    {
        memory().putFloat(offsetOf(index), value);
    }

    protected final double readDouble(long index)
    {
        return memory().getDouble(offsetOf(index));
    }

    protected final void writeDouble(long index, double value)
    {
        memory().putDouble(offsetOf(index), value);
    }

    private static ElementSize requireElementSize(ElementSize elementSize)
    {
        return Objects.requireNonNull(elementSize, "elementSize");
    }
}