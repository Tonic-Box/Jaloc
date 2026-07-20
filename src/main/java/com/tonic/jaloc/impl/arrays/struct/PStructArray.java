package com.tonic.jaloc.impl.arrays.struct;

import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractNativeArray;
import com.tonic.jaloc.memory.data.struct.StructField;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.data.struct.StructType;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class PStructArray<T extends PStruct> extends AbstractNativeArray<PStructWriter<T>> implements Iterable<T>
{
    private final StructLayout layout;
    private final StructViewFactory<T> viewFactory;

    public PStructArray(StructViewFactory<T> viewFactory, StructLayout layout, long length)
    {
        this(SystemAllocator.getInstance(), viewFactory, layout, length);
    }

    public PStructArray(NativeAllocator allocator, StructViewFactory<T> viewFactory, StructLayout layout, long length)
    {
        super(Objects.requireNonNull(allocator, "allocator"), length, totalByteSize(layout, length), requireLayout(layout).alignment());

        this.layout = layout;
        this.viewFactory = Objects.requireNonNull(viewFactory, "viewFactory");
    }

    public StructLayout layout()
    {
        return layout;
    }

    public long stride()
    {
        return layout.stride();
    }

    public boolean getBoolean(long index, StructField field)
    {
        long offset = fieldOffset(index, field, StructType.BOOLEAN);
        return UnsafeMemory.getByte(baseAddress() + offset) != 0;
    }

    public void setBoolean(long index, StructField field, boolean value)
    {
        long offset = fieldOffset(index, field, StructType.BOOLEAN);
        UnsafeMemory.putByte(baseAddress() + offset, value ? (byte) 1 : (byte) 0);
    }

    public byte getByte(long index, StructField field)
    {
        long offset = fieldOffset(index, field, StructType.BYTE);
        return UnsafeMemory.getByte(baseAddress() + offset);
    }

    public void setByte(long index, StructField field, byte value)
    {
        long offset = fieldOffset(index, field, StructType.BYTE);
        UnsafeMemory.putByte(baseAddress() + offset, value);
    }

    public short getShort(long index, StructField field)
    {
        long offset = fieldOffset(index, field, StructType.SHORT);
        return UnsafeMemory.getShort(baseAddress() + offset);
    }

    public void setShort(long index, StructField field, short value)
    {
        long offset = fieldOffset(index, field, StructType.SHORT);
        UnsafeMemory.putShort(baseAddress() + offset, value);
    }

    public char getChar(long index, StructField field)
    {
        long offset = fieldOffset(index, field, StructType.CHAR);
        return UnsafeMemory.getChar(baseAddress() + offset);
    }

    public void setChar(long index, StructField field, char value)
    {
        long offset = fieldOffset(index, field, StructType.CHAR);

        UnsafeMemory.putChar(baseAddress() + offset, value);
    }

    public int getInt(long index, StructField field)
    {
        long offset = fieldOffset(index, field, StructType.INT);
        return UnsafeMemory.getInt(baseAddress() + offset);
    }

    public void setInt(long index, StructField field, int value)
    {
        long offset = fieldOffset(index, field, StructType.INT);
        UnsafeMemory.putInt(baseAddress() + offset, value);
    }

    public long getLong(long index, StructField field)
    {
        long offset = fieldOffset(index, field, StructType.LONG);
        return UnsafeMemory.getLong(baseAddress() + offset);
    }

    public void setLong(long index, StructField field, long value)
    {
        long offset = fieldOffset(index, field, StructType.LONG);
        UnsafeMemory.putLong(baseAddress() + offset, value);
    }

    public float getFloat(long index, StructField field)
    {
        long offset = fieldOffset(index, field, StructType.FLOAT);
        return UnsafeMemory.getFloat(baseAddress() + offset);
    }

    public void setFloat(long index, StructField field, float value)
    {
        long offset = fieldOffset(index, field, StructType.FLOAT);
        UnsafeMemory.putFloat(baseAddress() + offset, value);
    }

    public double getDouble(long index, StructField field)
    {
        long offset = fieldOffset(index, field, StructType.DOUBLE);
        return UnsafeMemory.getDouble(baseAddress() + offset);
    }

    public void setDouble(long index, StructField field, double value)
    {
        long offset = fieldOffset(index, field, StructType.DOUBLE);
        UnsafeMemory.putDouble(baseAddress() + offset, value);
    }

    public void clearStruct(long index)
    {
        long offset = structOffset(index);
        UnsafeMemory.clear(baseAddress() + offset, layout.stride());
    }

    @Override
    public void clearRange(long fromIndex, long toIndex)
    {
        checkRange(fromIndex, toIndex);

        long count = toIndex - fromIndex;

        if (count == 0)
        {
            return;
        }

        UnsafeMemory.clear(baseAddress() + fromIndex * layout.stride(), count * layout.stride());
    }

    public void copyStruct(long sourceIndex, long destinationIndex)
    {
        long sourceOffset = structOffset(sourceIndex);
        long destinationOffset = structOffset(destinationIndex);

        UnsafeMemory.copy(baseAddress() + sourceOffset, baseAddress() + destinationOffset, layout.stride());
    }

    public void copyStructTo(long sourceIndex, PStructArray<?> destination, long destinationIndex)
    {
        Objects.requireNonNull(destination, "destination");

        if (layout != destination.layout)
        {
            throw new IllegalArgumentException("Struct arrays must use the same layout instance");
        }

        long sourceOffset = structOffset(sourceIndex);
        long destinationOffset = destination.structOffset(destinationIndex);

        UnsafeMemory.copy(baseAddress() + sourceOffset, destination.baseAddress() + destinationOffset, layout.stride());
    }

    @Override
    public PStructWriter<T> writer()
    {
        return new PStructWriter<>(this);
    }

    public T at(long index)
    {
        checkIndex(index);
        return viewFactory.create(this, index);
    }

    long structOffset(long index)
    {
        checkIndex(index);

        return index * layout.stride();
    }

    private long fieldOffset(long index, StructField field, StructType expectedType)
    {
        layout.validateField(field, expectedType);

        return structOffset(index) + field.getOffset();
    }

    private static StructLayout requireLayout(StructLayout layout)
    {
        return Objects.requireNonNull(layout, "layout");
    }

    private static long totalByteSize(StructLayout layout, long length)
    {
        StructLayout checkedLayout = requireLayout(layout);

        if (length < 0)
        {
            throw new IllegalArgumentException("length cannot be negative");
        }

        return Math.multiplyExact(checkedLayout.stride(), length);
    }

    @Override
    @NotNull
    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            private long index;

            @Override
            public boolean hasNext()
            {
                return index < length();
            }

            @Override
            public T next()
            {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                return at(index++);
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }
}