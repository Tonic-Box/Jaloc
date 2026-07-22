package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.impl.arrays.struct.PStruct;
import com.tonic.jaloc.impl.arrays.struct.PStructArray;
import com.tonic.jaloc.impl.arrays.struct.PStructWriter;
import com.tonic.jaloc.memory.data.struct.StructField;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.data.struct.StructType;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.HashMath;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

/**
 * The struct-entry hash engine behind the maps: open addressing over a keyed layout with a reserved zero-key slot.
 */
public abstract class AbstractNativeMap<T extends PStruct> extends AbstractNativeCollection<PStructArray<T>, PStructWriter<T>>
{
    private static final int KEY_BYTE = 0;
    private static final int KEY_SHORT = 1;
    private static final int KEY_CHAR = 2;
    private static final int KEY_INT = 3;
    private static final int KEY_LONG = 4;

    private final StructLayout layout;
    private final StructType keyType;
    private final long keyOffset;
    private final long stride;
    private final int keyKind;
    private final boolean integralKey;
    private final boolean floatKey;

    private long tableBase;
    private long tableMask;
    private long growLimit;
    private boolean hasZeroKey;

    protected AbstractNativeMap(NativeAllocator allocator, PStructArray<T> initialArray, String keyFieldName)
    {
        super(allocator, initialArray);

        this.layout = initialArray.layout();

        StructField keyField = layout.field(keyFieldName);

        this.keyType = keyField.getType();
        this.keyOffset = keyField.getOffset();
        this.stride = layout.stride();
        this.keyKind = kindOf(keyType);
        this.integralKey = keyType != StructType.FLOAT && keyType != StructType.DOUBLE;
        this.floatKey = keyType == StructType.FLOAT;
        this.tableBase = initialArray.baseAddress();
        this.tableMask = initialArray.length() - 2;
        this.growLimit = loadLimit();
    }

    /**
     * @return the entry layout
     */
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

            long position = HashMath.mix(bits) & destinationMask;

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

    /**
     * Empties the map.
     *
     * @throws IllegalStateException if closed
     */
    public void clear()
    {
        ensureOpen();

        elements().clear();
        hasZeroKey = false;
        size(0);
    }

    /**
     * Shrinks the table to the smallest size fitting the current mappings.
     *
     * @throws IllegalStateException if closed
     */
    public final void trim()
    {
        ensureOpen();

        long target = tableLength(occupancy());

        if (target >= elementsUnchecked().length())
        {
            return;
        }

        replaceArray(target);
    }

    protected final StructType keyType()
    {
        return keyType;
    }

    protected final long slotCount()
    {
        return tableMask + 1;
    }

    protected final long zeroSlot()
    {
        return tableMask + 1;
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
        size(sizeUnchecked() + 1);
        return ~slot;
    }

    protected final boolean removeZero()
    {
        if (!hasZeroKey)
        {
            return false;
        }

        UnsafeMemory.clear(tableBase + zeroSlot() * stride, stride);
        hasZeroKey = false;
        size(sizeUnchecked() - 1);
        return true;
    }

    /**
     * Tests whether key is present.
     *
     * @param key the key
     * @return true if present
     * @throws IllegalArgumentException on key type mismatch
     * @throws IllegalStateException if closed
     */
    public final boolean containsKey(long key)
    {
        ensureOpen();

        return findKey(integralKeyBits(key)) >= 0;
    }

    /**
     * Tests whether key is present.
     *
     * @param key the key
     * @return true if present
     * @throws IllegalArgumentException on key type mismatch
     * @throws IllegalStateException if closed
     */
    public final boolean containsKey(double key)
    {
        ensureOpen();

        return findKey(floatingKeyBits(key)) >= 0;
    }

    /**
     * Removes the mapping for key.
     *
     * @param key the key
     * @return true if the map changed
     * @throws IllegalArgumentException on key type mismatch
     * @throws IllegalStateException if closed
     */
    public final boolean remove(long key)
    {
        ensureOpen();

        long bits = integralKeyBits(key);

        return bits == 0 ? removeZero() : removeSlot(bits);
    }

    /**
     * Removes the mapping for key.
     *
     * @param key the key
     * @return true if the map changed
     * @throws IllegalArgumentException on key type mismatch
     * @throws IllegalStateException if closed
     */
    public final boolean remove(double key)
    {
        ensureOpen();

        long bits = floatingKeyBits(key);

        return bits == 0 ? removeZero() : removeSlot(bits);
    }

    protected final long findKey(long bits)
    {
        return bits == 0 ? (containsZeroKey() ? zeroSlot() : -1) : findSlot(bits);
    }

    protected final long insertKey(long bits)
    {
        long slot = bits == 0 ? zeroInsert() : insertSlot(bits);

        return slot < 0 ? ~slot : slot;
    }

    protected final long findSlot(long keyBits)
    {
        if (keyKind == KEY_LONG)
        {
            return findSlotLong(keyBits);
        }

        if (keyKind == KEY_INT)
        {
            return findSlotInt(keyBits);
        }

        return findSlotGeneric(keyBits);
    }

    private long findSlotLong(long keyBits)
    {
        long base = tableBase + keyOffset;
        long mask = tableMask;
        long position = HashMath.mix(keyBits) & mask;

        while (true)
        {
            long current = UnsafeMemory.getLong(base + position * stride);

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

    private long findSlotInt(long keyBits)
    {
        long base = tableBase + keyOffset;
        long mask = tableMask;
        long position = HashMath.mix(keyBits) & mask;
        int key = (int) keyBits;

        while (true)
        {
            int current = UnsafeMemory.getInt(base + position * stride);

            if (current == 0)
            {
                return -1;
            }

            if (current == key)
            {
                return position;
            }

            position = (position + 1) & mask;
        }
    }

    private long findSlotGeneric(long keyBits)
    {
        long base = tableBase;
        long mask = tableMask;
        long position = HashMath.mix(keyBits) & mask;

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
        if (keyKind == KEY_LONG)
        {
            return insertSlotLong(keyBits);
        }

        if (keyKind == KEY_INT)
        {
            return insertSlotInt(keyBits);
        }

        return insertSlotGeneric(keyBits);
    }

    private long insertSlotLong(long keyBits)
    {
        long base = tableBase + keyOffset;
        long entryStride = stride;
        long mask = tableMask;
        long position = HashMath.mix(keyBits) & mask;

        while (true)
        {
            long current = UnsafeMemory.getLong(base + position * entryStride);

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

        if (occupancy() + 1 > growLimit)
        {
            replaceArray(((tableMask + 1) << 1) + 1);

            base = tableBase + keyOffset;
            mask = tableMask;
            position = HashMath.mix(keyBits) & mask;

            while (UnsafeMemory.getLong(base + position * entryStride) != 0)
            {
                position = (position + 1) & mask;
            }
        }

        writeKeyAt(tableBase, position, keyBits);
        size(sizeUnchecked() + 1);
        return ~position;
    }

    private long insertSlotInt(long keyBits)
    {
        long base = tableBase + keyOffset;
        long entryStride = stride;
        long mask = tableMask;
        long position = HashMath.mix(keyBits) & mask;
        int key = (int) keyBits;

        while (true)
        {
            int current = UnsafeMemory.getInt(base + position * entryStride);

            if (current == 0)
            {
                break;
            }

            if (current == key)
            {
                return position;
            }

            position = (position + 1) & mask;
        }

        if (occupancy() + 1 > growLimit)
        {
            replaceArray(((tableMask + 1) << 1) + 1);

            base = tableBase + keyOffset;
            mask = tableMask;
            position = HashMath.mix(keyBits) & mask;

            while (UnsafeMemory.getInt(base + position * entryStride) != 0)
            {
                position = (position + 1) & mask;
            }
        }

        writeKeyAt(tableBase, position, keyBits);
        size(sizeUnchecked() + 1);
        return ~position;
    }

    private long insertSlotGeneric(long keyBits)
    {
        long base = tableBase;
        long mask = tableMask;
        long position = HashMath.mix(keyBits) & mask;

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

        if (occupancy() + 1 > growLimit)
        {
            replaceArray(((tableMask + 1) << 1) + 1);

            base = tableBase;
            mask = tableMask;
            position = HashMath.mix(keyBits) & mask;

            while (keyBitsAt(base, position) != 0)
            {
                position = (position + 1) & mask;
            }
        }

        writeKeyAt(base, position, keyBits);
        size(sizeUnchecked() + 1);
        return ~position;
    }

    protected final boolean removeSlot(long keyBits)
    {
        long base = tableBase;
        long mask = tableMask;
        long position = HashMath.mix(keyBits) & mask;

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
                size(sizeUnchecked() - 1);
                return true;
            }

            position = (position + 1) & mask;
        }
    }

    protected final long keyBitsAt(long slot)
    {
        return keyBitsAt(tableBase, slot);
    }

    protected final long slotAddress(long slot)
    {
        return tableBase + slot * stride;
    }

    protected final long integralKeyBits(long key)
    {
        if (!integralKey)
        {
            throw new IllegalArgumentException("key field is " + keyType + ", not integral");
        }

        switch (keyKind)
        {
            case KEY_BYTE:
                if (key != (byte) key)
                {
                    throw new IllegalArgumentException("key does not fit BYTE: " + key);
                }
                return key;
            case KEY_SHORT:
                if (key != (short) key)
                {
                    throw new IllegalArgumentException("key does not fit SHORT: " + key);
                }
                return key;
            case KEY_CHAR:
                if (key != (key & 0xFFFFL))
                {
                    throw new IllegalArgumentException("key does not fit CHAR: " + key);
                }
                return key;
            case KEY_INT:
                if (key != (int) key)
                {
                    throw new IllegalArgumentException("key does not fit INT: " + key);
                }
                return key;
            default:
                return key;
        }
    }

    protected final long floatingKeyBits(double key)
    {
        if (integralKey)
        {
            throw new IllegalArgumentException("key field is " + keyType + ", not floating");
        }

        if (floatKey)
        {
            return Float.floatToIntBits((float) key);
        }

        return Double.doubleToLongBits(key);
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

                long slot = HashMath.mix(current) & mask;

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

        switch (keyKind)
        {
            case KEY_BYTE:
                return UnsafeMemory.getByte(address);
            case KEY_SHORT:
                return UnsafeMemory.getShort(address);
            case KEY_CHAR:
                return UnsafeMemory.getChar(address);
            case KEY_INT:
                return UnsafeMemory.getInt(address);
            default:
                return UnsafeMemory.getLong(address);
        }
    }

    private void writeKeyAt(long base, long slot, long keyBits)
    {
        long address = base + slot * stride + keyOffset;

        switch (keyKind)
        {
            case KEY_BYTE:
                UnsafeMemory.putByte(address, (byte) keyBits);
                return;
            case KEY_SHORT:
                UnsafeMemory.putShort(address, (short) keyBits);
                return;
            case KEY_CHAR:
                UnsafeMemory.putChar(address, (char) keyBits);
                return;
            case KEY_INT:
                UnsafeMemory.putInt(address, (int) keyBits);
                return;
            default:
                UnsafeMemory.putLong(address, keyBits);
        }
    }

    private long occupancy()
    {
        return sizeUnchecked() - (hasZeroKey ? 1 : 0);
    }

    private long loadLimit()
    {
        return HashMath.loadLimit(tableMask + 1);
    }

    @Override
    protected void onArrayReplaced()
    {
        super.onArrayReplaced();

        tableBase = elements().baseAddress();
        tableMask = elements().length() - 2;
        growLimit = loadLimit();
    }

    private static int kindOf(StructType keyType)
    {
        switch (keyType)
        {
            case BYTE:
                return KEY_BYTE;
            case SHORT:
                return KEY_SHORT;
            case CHAR:
                return KEY_CHAR;
            case INT:
            case FLOAT:
                return KEY_INT;
            case LONG:
            case DOUBLE:
                return KEY_LONG;
            default:
                throw new IllegalArgumentException("key field cannot be BOOLEAN");
        }
    }

    protected static long tableLength(long expectedElements)
    {
        return HashMath.tableSize(expectedElements) + 1;
    }
}
