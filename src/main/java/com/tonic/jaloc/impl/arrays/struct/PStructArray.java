package com.tonic.jaloc.impl.arrays.struct;

import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractNativeArray;
import com.tonic.jaloc.memory.data.struct.StructField;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.data.struct.StructType;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
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

    @Override
    protected long byteSize(long elementCount)
    {
        return Math.multiplyExact(layout.stride(), elementCount);
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

    public void swapStruct(long firstIndex, long secondIndex)
    {
        long firstOffset = structOffset(firstIndex);
        long secondOffset = structOffset(secondIndex);

        if (firstOffset == secondOffset)
        {
            return;
        }

        long first = baseAddress() + firstOffset;
        long second = baseAddress() + secondOffset;
        long stride = layout.stride();
        int alignment = layout.alignment();

        if (alignment == 8)
        {
            for (long offset = 0; offset < stride; offset += Long.BYTES)
            {
                long temp = UnsafeMemory.getLong(first + offset);
                UnsafeMemory.putLong(first + offset, UnsafeMemory.getLong(second + offset));
                UnsafeMemory.putLong(second + offset, temp);
            }
        }
        else if (alignment == 4)
        {
            for (long offset = 0; offset < stride; offset += Integer.BYTES)
            {
                int temp = UnsafeMemory.getInt(first + offset);
                UnsafeMemory.putInt(first + offset, UnsafeMemory.getInt(second + offset));
                UnsafeMemory.putInt(second + offset, temp);
            }
        }
        else if (alignment == 2)
        {
            for (long offset = 0; offset < stride; offset += Short.BYTES)
            {
                short temp = UnsafeMemory.getShort(first + offset);
                UnsafeMemory.putShort(first + offset, UnsafeMemory.getShort(second + offset));
                UnsafeMemory.putShort(second + offset, temp);
            }
        }
        else
        {
            for (long offset = 0; offset < stride; offset++)
            {
                byte temp = UnsafeMemory.getByte(first + offset);
                UnsafeMemory.putByte(first + offset, UnsafeMemory.getByte(second + offset));
                UnsafeMemory.putByte(second + offset, temp);
            }
        }
    }

    public void sort(Comparator<? super T> comparator)
    {
        sort(0, length(), comparator);
    }

    public void sort(long fromIndex, long toIndex, Comparator<? super T> comparator)
    {
        Objects.requireNonNull(comparator, "comparator");
        checkRange(fromIndex, toIndex);

        quicksort(fromIndex, toIndex - 1, comparator, cursor(), cursor());
    }

    private void quicksort(long low, long high, Comparator<? super T> comparator, T first, T second)
    {
        while (high - low >= 16)
        {
            long middle = low + ((high - low) >>> 1);

            swapStruct(medianOfThree(low, middle, high, comparator, first, second), high);

            long lessThan = low;
            long current = low;
            long greaterThan = high - 1;

            while (current <= greaterThan)
            {
                int comparison = compareAt(current, high, comparator, first, second);

                if (comparison < 0)
                {
                    swapStruct(current, lessThan);
                    lessThan++;
                    current++;
                }
                else if (comparison > 0)
                {
                    swapStruct(current, greaterThan);
                    greaterThan--;
                }
                else
                {
                    current++;
                }
            }

            swapStruct(greaterThan + 1, high);

            long leftHigh = lessThan - 1;
            long rightLow = greaterThan + 2;

            if (leftHigh - low < high - rightLow)
            {
                quicksort(low, leftHigh, comparator, first, second);
                low = rightLow;
            }
            else
            {
                quicksort(rightLow, high, comparator, first, second);
                high = leftHigh;
            }
        }

        insertionSort(low, high, comparator, first, second);
    }

    private void insertionSort(long low, long high, Comparator<? super T> comparator, T first, T second)
    {
        for (long i = low + 1; i <= high; i++)
        {
            for (long j = i; j > low; j--)
            {
                if (compareAt(j - 1, j, comparator, first, second) <= 0)
                {
                    break;
                }

                swapStruct(j - 1, j);
            }
        }
    }

    private long medianOfThree(long a, long b, long c, Comparator<? super T> comparator, T first, T second)
    {
        if (compareAt(a, b, comparator, first, second) < 0)
        {
            return compareAt(c, a, comparator, first, second) < 0 ? a : (compareAt(c, b, comparator, first, second) < 0 ? c : b);
        }

        return compareAt(c, b, comparator, first, second) < 0 ? b : (compareAt(c, a, comparator, first, second) < 0 ? c : a);
    }

    private int compareAt(long firstIndex, long secondIndex, Comparator<? super T> comparator, T first, T second)
    {
        first.moveTo(firstIndex);
        second.moveTo(secondIndex);

        return comparator.compare(first, second);
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

    public T cursor()
    {
        return viewFactory.create(this, 0);
    }

    void checkStructIndex(long index)
    {
        checkIndex(index);
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

    public Iterator<T> cursorIterator()
    {
        return new Iterator<T>()
        {
            private final T cursor = cursor();
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

                cursor.moveTo(index++);
                return cursor;
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }
}