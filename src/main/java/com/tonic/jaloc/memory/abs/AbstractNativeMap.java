package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.impl.arrays.struct.PStruct;
import com.tonic.jaloc.impl.arrays.struct.PStructArray;
import com.tonic.jaloc.impl.arrays.struct.PStructWriter;
import com.tonic.jaloc.memory.data.struct.StructField;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.data.struct.StructType;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

public abstract class AbstractNativeMap<T extends PStruct> extends AbstractNativeCollection<PStructArray<T>, PStructWriter<T>>
{
    private final StructLayout layout;
    private final StructField keyField;
    private final StructType keyType;
    private final long keyOffset;
    private final long stride;

    private boolean hasZeroKey;

    protected AbstractNativeMap(NativeAllocator allocator, PStructArray<T> initialArray, String keyFieldName)
    {
        super(allocator, initialArray);

        this.layout = initialArray.layout();
        this.keyField = layout.field(keyFieldName);
        this.keyType = keyField.getType();
        this.keyOffset = keyField.getOffset();
        this.stride = layout.stride();

        if (keyType == StructType.BOOLEAN)
        {
            throw new IllegalArgumentException("key field cannot be BOOLEAN");
        }
    }

    public final StructLayout layout()
    {
        return layout;
    }

    @Override
    protected void migrateElements(PStructArray<T> source, PStructArray<T> destination)
    {
        long sourceSlots = source.length() - 1;
        long sourceBase = source.baseAddress();
        long destinationBase = destination.baseAddress();
        long destinationMask = destination.length() - 2;

        for (long i = 0; i < sourceSlots; i++)
        {
            long bits = keyBitsAt(sourceBase, i);

            if (bits == 0)
            {
                continue;
            }

            long position = mix(bits) & destinationMask;

            while (keyBitsAt(destinationBase, position) != 0)
            {
                position = (position + 1) & destinationMask;
            }

            UnsafeMemory.copy(sourceBase + i * stride, destinationBase + position * stride, stride);
        }

        if (hasZeroKey)
        {
            UnsafeMemory.copy(sourceBase + sourceSlots * stride, destinationBase + (destination.length() - 1) * stride, stride);
        }
    }

    public void clear()
    {
        ensureOpen();

        elements().clear();
        hasZeroKey = false;
        size(0);
    }

    protected final StructType keyType()
    {
        return keyType;
    }

    protected final long slotCount()
    {
        return capacity() - 1;
    }

    protected final long zeroSlot()
    {
        return capacity() - 1;
    }

    protected final boolean containsZeroKey()
    {
        return hasZeroKey;
    }

    protected final long zeroInsert()
    {
        long slot = zeroSlot();

        if (hasZeroKey)
        {
            return slot;
        }

        hasZeroKey = true;
        size(size() + 1);
        return ~slot;
    }

    protected final boolean removeZero()
    {
        if (!hasZeroKey)
        {
            return false;
        }

        UnsafeMemory.clear(elements().baseAddress() + zeroSlot() * stride, stride);
        hasZeroKey = false;
        size(size() - 1);
        return true;
    }

    protected final long findSlot(long keyBits)
    {
        PStructArray<T> table = elements();
        long base = table.baseAddress();
        long mask = table.length() - 2;
        long position = mix(keyBits) & mask;

        while (true)
        {
            long current = keyBitsAt(base, position);

            if (current == 0)
            {
                return -1;
            }

            if (current == keyBits)
            {
                return position;
            }

            position = (position + 1) & mask;
        }
    }

    protected final long insertSlot(long keyBits)
    {
        PStructArray<T> table = elements();
        long base = table.baseAddress();
        long mask = table.length() - 2;
        long position = mix(keyBits) & mask;

        while (true)
        {
            long current = keyBitsAt(base, position);

            if (current == 0)
            {
                break;
            }

            if (current == keyBits)
            {
                return position;
            }

            position = (position + 1) & mask;
        }

        if (occupancy() + 1 > loadLimit())
        {
            replaceArray(((capacity() - 1) << 1) + 1);

            table = elements();
            base = table.baseAddress();
            mask = table.length() - 2;
            position = mix(keyBits) & mask;

            while (keyBitsAt(base, position) != 0)
            {
                position = (position + 1) & mask;
            }
        }

        writeKeyAt(base, position, keyBits);
        size(size() + 1);
        return ~position;
    }

    protected final boolean removeSlot(long keyBits)
    {
        PStructArray<T> table = elements();
        long base = table.baseAddress();
        long mask = table.length() - 2;
        long position = mix(keyBits) & mask;

        while (true)
        {
            long current = keyBitsAt(base, position);

            if (current == 0)
            {
                return false;
            }

            if (current == keyBits)
            {
                shiftEntries(base, position, mask);
                size(size() - 1);
                return true;
            }

            position = (position + 1) & mask;
        }
    }

    protected final long keyBitsAt(long slot)
    {
        return keyBitsAt(elements().baseAddress(), slot);
    }

    protected final long integralKeyBits(long key)
    {
        switch (keyType)
        {
            case BYTE:
                if (key != (byte) key)
                {
                    throw new IllegalArgumentException("key does not fit BYTE: " + key);
                }
                return key;
            case SHORT:
                if (key != (short) key)
                {
                    throw new IllegalArgumentException("key does not fit SHORT: " + key);
                }
                return key;
            case CHAR:
                if (key != (key & 0xFFFFL))
                {
                    throw new IllegalArgumentException("key does not fit CHAR: " + key);
                }
                return key;
            case INT:
                if (key != (int) key)
                {
                    throw new IllegalArgumentException("key does not fit INT: " + key);
                }
                return key;
            case LONG:
                return key;
            default:
                throw new IllegalArgumentException("key field is " + keyType + ", not integral");
        }
    }

    protected final long floatingKeyBits(double key)
    {
        if (keyType == StructType.FLOAT)
        {
            return Float.floatToIntBits((float) key);
        }

        if (keyType == StructType.DOUBLE)
        {
            return Double.doubleToLongBits(key);
        }

        throw new IllegalArgumentException("key field is " + keyType + ", not floating");
    }

    private void shiftEntries(long base, long position, long mask)
    {
        while (true)
        {
            long last = position;

            position = (position + 1) & mask;

            long current;

            while (true)
            {
                current = keyBitsAt(base, position);

                if (current == 0)
                {
                    UnsafeMemory.clear(base + last * stride, stride);
                    return;
                }

                long slot = mix(current) & mask;

                if (last <= position ? (last >= slot || slot > position) : (last >= slot && slot > position))
                {
                    break;
                }

                position = (position + 1) & mask;
            }

            UnsafeMemory.copy(base + position * stride, base + last * stride, stride);
        }
    }

    private long keyBitsAt(long base, long slot)
    {
        long address = base + slot * stride + keyOffset;

        switch (keyType)
        {
            case BYTE:
                return UnsafeMemory.getByte(address);
            case SHORT:
                return UnsafeMemory.getShort(address);
            case CHAR:
                return UnsafeMemory.getChar(address);
            case INT:
            case FLOAT:
                return UnsafeMemory.getInt(address);
            case LONG:
            case DOUBLE:
                return UnsafeMemory.getLong(address);
            default:
                throw new IllegalStateException("Unsupported key type: " + keyType);
        }
    }

    private void writeKeyAt(long base, long slot, long keyBits)
    {
        long address = base + slot * stride + keyOffset;

        switch (keyType)
        {
            case BYTE:
                UnsafeMemory.putByte(address, (byte) keyBits);
                return;
            case SHORT:
                UnsafeMemory.putShort(address, (short) keyBits);
                return;
            case CHAR:
                UnsafeMemory.putChar(address, (char) keyBits);
                return;
            case INT:
            case FLOAT:
                UnsafeMemory.putInt(address, (int) keyBits);
                return;
            case LONG:
            case DOUBLE:
                UnsafeMemory.putLong(address, keyBits);
                return;
            default:
                throw new IllegalStateException("Unsupported key type: " + keyType);
        }
    }

    private long occupancy()
    {
        return size() - (hasZeroKey ? 1 : 0);
    }

    private long loadLimit()
    {
        long tableSize = capacity() - 1;

        return tableSize - (tableSize >>> 2);
    }

    private static long mix(long value)
    {
        long hash = value * 0x9E3779B97F4A7C15L;

        return hash ^ (hash >>> 32);
    }

    protected static long tableLength(long expectedElements)
    {
        if (expectedElements < 0)
        {
            throw new IllegalArgumentException("expectedElements cannot be negative");
        }

        long needed = expectedElements + ((expectedElements + 2) / 3);

        return nextPowerOfTwo(Math.max(16, needed)) + 1;
    }

    private static long nextPowerOfTwo(long value)
    {
        long highest = Long.highestOneBit(value);

        return highest == value ? value : highest << 1;
    }
}
