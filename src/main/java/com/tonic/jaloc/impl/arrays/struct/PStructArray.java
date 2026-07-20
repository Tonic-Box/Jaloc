package com.tonic.jaloc.impl.arrays.struct;

import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractNativeArray;
import com.tonic.jaloc.memory.data.struct.StructField;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.data.struct.StructType;
import com.tonic.jaloc.memory.iface.NativeAllocator;
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
        return memory().getByte(offset) != 0;
    }

    public void setBoolean(long index, StructField field, boolean value)
    {
        long offset = fieldOffset(index, field, StructType.BOOLEAN);
        memory().putByte(offset, value ? (byte) 1 : (byte) 0);
    }

    public byte getByte(long index, StructField field)
    {
        long offset = fieldOffset(index, field, StructType.BYTE);
        return memory().getByte(offset);
    }

    public void setByte(long index, StructField field, byte value)
    {
        long offset = fieldOffset(index, field, StructType.BYTE);
        memory().putByte(offset, value);
    }

    public short getShort(long index, StructField field)
    {
        long offset = fieldOffset(index, field, StructType.SHORT);
        return memory().getShort(offset);
    }

    public void setShort(long index, StructField field, short value)
    {
        long offset = fieldOffset(index, field, StructType.SHORT);
        memory().putShort(offset, value);
    }

    public char getChar(long index, StructField field)
    {
        long offset = fieldOffset(index, field, StructType.CHAR);
        return memory().getChar(offset);
    }

    public void setChar(long index, StructField field, char value)
    {
        long offset = fieldOffset(index, field, StructType.CHAR);

        memory().putChar(offset, value);
    }

    public int getInt(long index, StructField field)
    {
        long offset = fieldOffset(index, field, StructType.INT);
        return memory().getInt(offset);
    }

    public void setInt(long index, StructField field, int value)
    {
        long offset = fieldOffset(index, field, StructType.INT);
        memory().putInt(offset, value);
    }

    public long getLong(long index, StructField field)
    {
        long offset = fieldOffset(index, field, StructType.LONG);
        return memory().getLong(offset);
    }

    public void setLong(long index, StructField field, long value)
    {
        long offset = fieldOffset(index, field, StructType.LONG);
        memory().putLong(offset, value);
    }

    public float getFloat(long index, StructField field)
    {
        long offset = fieldOffset(index, field, StructType.FLOAT);
        return memory().getFloat(offset);
    }

    public void setFloat(long index, StructField field, float value)
    {
        long offset = fieldOffset(index, field, StructType.FLOAT);
        memory().putFloat(offset, value);
    }

    public double getDouble(long index, StructField field)
    {
        long offset = fieldOffset(index, field, StructType.DOUBLE);
        return memory().getDouble(offset);
    }

    public void setDouble(long index, StructField field, double value)
    {
        long offset = fieldOffset(index, field, StructType.DOUBLE);
        memory().putDouble(offset, value);
    }

    public void clearStruct(long index)
    {
        long offset = structOffset(index);
        memory().slice(offset, layout.stride()).clear();
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

        long offset = Math.multiplyExact(fromIndex, layout.stride());

        long bytes = Math.multiplyExact(count, layout.stride());

        memory().slice(offset, bytes).clear();
    }

    public void copyStruct(long sourceIndex, long destinationIndex)
    {
        long sourceOffset = structOffset(sourceIndex);
        long destinationOffset = structOffset(destinationIndex);

        memory().copyTo(sourceOffset, memory(), destinationOffset, layout.stride());
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

        memory().copyTo(sourceOffset, destination.memory(), destinationOffset, layout.stride());
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

        return Math.multiplyExact(index, layout.stride());
    }

    private long fieldOffset(long index, StructField field, StructType expectedType)
    {
        layout.validateField(field, expectedType);

        return Math.addExact(structOffset(index), field.getOffset());
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