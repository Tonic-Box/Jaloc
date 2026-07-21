package com.tonic.jaloc.impl.maps;


import com.tonic.jaloc.impl.arrays.struct.PStruct;
import com.tonic.jaloc.impl.arrays.struct.PStructArray;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.AbstractNativeMap;
import com.tonic.jaloc.memory.data.struct.StructField;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.data.struct.StructType;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.DoubleConsumer;
import java.util.function.LongConsumer;

/**
 * A native primitive-to-primitive hash map; the StructType pair fixes the key and value types, K and V are declaration metadata only.
 */
public final class PMap<K, V> extends AbstractNativeMap<PMap.Slot>
{
    private static final int VALUE_BYTE = 0;
    private static final int VALUE_SHORT = 1;
    private static final int VALUE_CHAR = 2;
    private static final int VALUE_INT = 3;
    private static final int VALUE_LONG = 4;

    private final StructField valueField;
    private final StructType valueType;
    private final long valueOffset;
    private final int valueKind;
    private final boolean integralValue;

    /**
     * Creates an empty map for the given key and value types on the system allocator.
     *
     * @param keyType the key type, any StructType but BOOLEAN
     * @param valueType the value type
     * @throws IllegalArgumentException if keyType is BOOLEAN
     */
    public PMap(StructType keyType, StructType valueType)
    {
        this(SystemAllocator.getInstance(), keyType, valueType, 0);
    }

    /**
     * Creates an empty map presized for expectedElements on the system allocator.
     *
     * @param keyType the key type, any StructType but BOOLEAN
     * @param valueType the value type
     * @param expectedElements presizes the table
     * @throws IllegalArgumentException if keyType is BOOLEAN or expectedElements is negative
     */
    public PMap(StructType keyType, StructType valueType, long expectedElements)
    {
        this(SystemAllocator.getInstance(), keyType, valueType, expectedElements);
    }

    /**
     * Creates an empty map presized for expectedElements on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param keyType the key type, any StructType but BOOLEAN
     * @param valueType the value type
     * @param expectedElements presizes the table
     * @throws IllegalArgumentException if keyType is BOOLEAN or expectedElements is negative
     */
    public PMap(NativeAllocator allocator, StructType keyType, StructType valueType, long expectedElements)
    {
        super(Objects.requireNonNull(allocator, "allocator"), new PStructArray<>(allocator, Slot::new, buildLayout(keyType, valueType), tableLength(expectedElements)), "key");

        this.valueField = layout().field("value");
        this.valueType = valueField.getType();
        this.valueOffset = valueField.getOffset();
        this.valueKind = valueKindOf(valueType);
        this.integralValue = valueKind >= 0;
    }

    @Override
    protected PStructArray<Slot> createArray(NativeAllocator allocator, long capacity)
    {
        return new PStructArray<>(allocator, Slot::new, layout(), capacity);
    }

    /**
     * Maps key to value, replacing any existing mapping.
     *
     * @param key the key
     * @param value the value
     * @throws IllegalArgumentException if key or value does not fit its declared type
     * @throws IllegalStateException if closed
     */
    public void put(long key, long value)
    {
        ensureOpen();
        validateIntegralValue(value);
        writeIntegralValue(insertKey(integralKeyBits(key)), value);
    }

    /**
     * Maps key to value, replacing any existing mapping.
     *
     * @param key the key
     * @param value the value
     * @throws IllegalArgumentException if key or value does not fit its declared type
     * @throws IllegalStateException if closed
     */
    public void put(long key, double value)
    {
        ensureOpen();
        requireFloatingValue();
        writeFloatingValue(insertKey(integralKeyBits(key)), value);
    }

    /**
     * Maps key to value, replacing any existing mapping.
     *
     * @param key the key
     * @param value the value
     * @throws IllegalArgumentException if key or value does not fit its declared type
     * @throws IllegalStateException if closed
     */
    public void put(long key, boolean value)
    {
        ensureOpen();
        requireValueType(StructType.BOOLEAN);
        UnsafeMemory.putByte(valueAddress(insertKey(integralKeyBits(key))), value ? (byte) 1 : (byte) 0);
    }

    /**
     * Maps key to value, replacing any existing mapping.
     *
     * @param key the key
     * @param value the value
     * @throws IllegalArgumentException if key or value does not fit its declared type
     * @throws IllegalStateException if closed
     */
    public void put(double key, long value)
    {
        ensureOpen();
        validateIntegralValue(value);
        writeIntegralValue(insertKey(floatingKeyBits(key)), value);
    }

    /**
     * Maps key to value, replacing any existing mapping.
     *
     * @param key the key
     * @param value the value
     * @throws IllegalArgumentException if key or value does not fit its declared type
     * @throws IllegalStateException if closed
     */
    public void put(double key, double value)
    {
        ensureOpen();
        requireFloatingValue();
        writeFloatingValue(insertKey(floatingKeyBits(key)), value);
    }

    /**
     * Maps key to value, replacing any existing mapping.
     *
     * @param key the key
     * @param value the value
     * @throws IllegalArgumentException if key or value does not fit its declared type
     * @throws IllegalStateException if closed
     */
    public void put(double key, boolean value)
    {
        ensureOpen();
        requireValueType(StructType.BOOLEAN);
        UnsafeMemory.putByte(valueAddress(insertKey(floatingKeyBits(key))), value ? (byte) 1 : (byte) 0);
    }

    /**
     * Reads the value for key.
     *
     * @param key the key
     * @return the value
     * @throws NoSuchElementException if key is absent
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public byte getByte(long key)
    {
        ensureOpen();
        requireValueType(StructType.BYTE);
        return UnsafeMemory.getByte(valueAddress(requireSlot(integralKeyBits(key), key)));
    }

    /**
     * Reads the value for key.
     *
     * @param key the key
     * @return the value
     * @throws NoSuchElementException if key is absent
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public byte getByte(double key)
    {
        ensureOpen();
        requireValueType(StructType.BYTE);
        return UnsafeMemory.getByte(valueAddress(requireSlot(floatingKeyBits(key), key)));
    }

    /**
     * Reads the value for key.
     *
     * @param key the key
     * @return the value
     * @throws NoSuchElementException if key is absent
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public short getShort(long key)
    {
        ensureOpen();
        requireValueType(StructType.SHORT);
        return UnsafeMemory.getShort(valueAddress(requireSlot(integralKeyBits(key), key)));
    }

    /**
     * Reads the value for key.
     *
     * @param key the key
     * @return the value
     * @throws NoSuchElementException if key is absent
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public short getShort(double key)
    {
        ensureOpen();
        requireValueType(StructType.SHORT);
        return UnsafeMemory.getShort(valueAddress(requireSlot(floatingKeyBits(key), key)));
    }

    /**
     * Reads the value for key.
     *
     * @param key the key
     * @return the value
     * @throws NoSuchElementException if key is absent
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public char getChar(long key)
    {
        ensureOpen();
        requireValueType(StructType.CHAR);
        return UnsafeMemory.getChar(valueAddress(requireSlot(integralKeyBits(key), key)));
    }

    /**
     * Reads the value for key.
     *
     * @param key the key
     * @return the value
     * @throws NoSuchElementException if key is absent
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public char getChar(double key)
    {
        ensureOpen();
        requireValueType(StructType.CHAR);
        return UnsafeMemory.getChar(valueAddress(requireSlot(floatingKeyBits(key), key)));
    }

    /**
     * Reads the value for key.
     *
     * @param key the key
     * @return the value
     * @throws NoSuchElementException if key is absent
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public int getInt(long key)
    {
        ensureOpen();
        requireValueType(StructType.INT);
        return UnsafeMemory.getInt(valueAddress(requireSlot(integralKeyBits(key), key)));
    }

    /**
     * Reads the value for key.
     *
     * @param key the key
     * @return the value
     * @throws NoSuchElementException if key is absent
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public int getInt(double key)
    {
        ensureOpen();
        requireValueType(StructType.INT);
        return UnsafeMemory.getInt(valueAddress(requireSlot(floatingKeyBits(key), key)));
    }

    /**
     * Reads the value for key.
     *
     * @param key the key
     * @return the value
     * @throws NoSuchElementException if key is absent
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public long getLong(long key)
    {
        ensureOpen();
        requireValueType(StructType.LONG);
        return UnsafeMemory.getLong(valueAddress(requireSlot(integralKeyBits(key), key)));
    }

    /**
     * Reads the value for key.
     *
     * @param key the key
     * @return the value
     * @throws NoSuchElementException if key is absent
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public long getLong(double key)
    {
        ensureOpen();
        requireValueType(StructType.LONG);
        return UnsafeMemory.getLong(valueAddress(requireSlot(floatingKeyBits(key), key)));
    }

    /**
     * Reads the value for key.
     *
     * @param key the key
     * @return the value
     * @throws NoSuchElementException if key is absent
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public float getFloat(long key)
    {
        ensureOpen();
        requireValueType(StructType.FLOAT);
        return UnsafeMemory.getFloat(valueAddress(requireSlot(integralKeyBits(key), key)));
    }

    /**
     * Reads the value for key.
     *
     * @param key the key
     * @return the value
     * @throws NoSuchElementException if key is absent
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public float getFloat(double key)
    {
        ensureOpen();
        requireValueType(StructType.FLOAT);
        return UnsafeMemory.getFloat(valueAddress(requireSlot(floatingKeyBits(key), key)));
    }

    /**
     * Reads the value for key.
     *
     * @param key the key
     * @return the value
     * @throws NoSuchElementException if key is absent
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public double getDouble(long key)
    {
        ensureOpen();
        requireValueType(StructType.DOUBLE);
        return UnsafeMemory.getDouble(valueAddress(requireSlot(integralKeyBits(key), key)));
    }

    /**
     * Reads the value for key.
     *
     * @param key the key
     * @return the value
     * @throws NoSuchElementException if key is absent
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public double getDouble(double key)
    {
        ensureOpen();
        requireValueType(StructType.DOUBLE);
        return UnsafeMemory.getDouble(valueAddress(requireSlot(floatingKeyBits(key), key)));
    }

    /**
     * Reads the value for key.
     *
     * @param key the key
     * @return the value
     * @throws NoSuchElementException if key is absent
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public boolean getBool(long key)
    {
        ensureOpen();
        requireValueType(StructType.BOOLEAN);
        return UnsafeMemory.getByte(valueAddress(requireSlot(integralKeyBits(key), key))) != 0;
    }

    /**
     * Reads the value for key.
     *
     * @param key the key
     * @return the value
     * @throws NoSuchElementException if key is absent
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public boolean getBool(double key)
    {
        ensureOpen();
        requireValueType(StructType.BOOLEAN);
        return UnsafeMemory.getByte(valueAddress(requireSlot(floatingKeyBits(key), key))) != 0;
    }

    /**
     * Reads the value for key, or defaultValue when absent.
     *
     * @param key the key
     * @param defaultValue the fallback
     * @return the value, or defaultValue
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public long getOrDefault(long key, long defaultValue)
    {
        ensureOpen();
        requireIntegralValue();

        long slot = findKey(integralKeyBits(key));

        return slot < 0 ? defaultValue : readIntegralValue(slot);
    }

    /**
     * Reads the value for key, or defaultValue when absent.
     *
     * @param key the key
     * @param defaultValue the fallback
     * @return the value, or defaultValue
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public long getOrDefault(double key, long defaultValue)
    {
        ensureOpen();
        requireIntegralValue();

        long slot = findKey(floatingKeyBits(key));

        return slot < 0 ? defaultValue : readIntegralValue(slot);
    }

    /**
     * Reads the value for key, or defaultValue when absent.
     *
     * @param key the key
     * @param defaultValue the fallback
     * @return the value, or defaultValue
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public double getOrDefault(long key, double defaultValue)
    {
        ensureOpen();
        requireFloatingValue();

        long slot = findKey(integralKeyBits(key));

        return slot < 0 ? defaultValue : readFloatingValue(slot);
    }

    /**
     * Reads the value for key, or defaultValue when absent.
     *
     * @param key the key
     * @param defaultValue the fallback
     * @return the value, or defaultValue
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public double getOrDefault(double key, double defaultValue)
    {
        ensureOpen();
        requireFloatingValue();

        long slot = findKey(floatingKeyBits(key));

        return slot < 0 ? defaultValue : readFloatingValue(slot);
    }

    /**
     * Reads the value for key, or defaultValue when absent.
     *
     * @param key the key
     * @param defaultValue the fallback
     * @return the value, or defaultValue
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public boolean getOrDefault(long key, boolean defaultValue)
    {
        ensureOpen();
        requireValueType(StructType.BOOLEAN);

        long slot = findKey(integralKeyBits(key));

        return slot < 0 ? defaultValue : UnsafeMemory.getByte(valueAddress(slot)) != 0;
    }

    /**
     * Reads the value for key, or defaultValue when absent.
     *
     * @param key the key
     * @param defaultValue the fallback
     * @return the value, or defaultValue
     * @throws IllegalArgumentException on key or value type mismatch
     * @throws IllegalStateException if closed
     */
    public boolean getOrDefault(double key, boolean defaultValue)
    {
        ensureOpen();
        requireValueType(StructType.BOOLEAN);

        long slot = findKey(floatingKeyBits(key));

        return slot < 0 ? defaultValue : UnsafeMemory.getByte(valueAddress(slot)) != 0;
    }

    /**
     * Tests whether key is present.
     *
     * @param key the key
     * @return true if present
     * @throws IllegalArgumentException on key type mismatch
     * @throws IllegalStateException if closed
     */
    public boolean containsKey(long key)
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
    public boolean containsKey(double key)
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
    public boolean remove(long key)
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
    public boolean remove(double key)
    {
        ensureOpen();

        long bits = floatingKeyBits(key);

        return bits == 0 ? removeZero() : removeSlot(bits);
    }

    /**
     * Emits every key; integral-keyed maps only.
     *
     * @param consumer the receiver
     * @throws NullPointerException if consumer is null
     * @throws IllegalArgumentException if the key type is floating
     * @throws IllegalStateException if closed
     */
    public void forEachKey(LongConsumer consumer)
    {
        Objects.requireNonNull(consumer, "consumer");
        ensureOpen();
        requireIntegralKey();

        long slots = slotCount();

        for (long slot = 0; slot < slots; slot++)
        {
            long bits = keyBitsAt(slot);

            if (bits != 0)
            {
                consumer.accept(bits);
            }
        }

        if (containsZeroKey())
        {
            consumer.accept(0);
        }
    }

    /**
     * Emits every key; floating-keyed maps only.
     *
     * @param consumer the receiver
     * @throws NullPointerException if consumer is null
     * @throws IllegalArgumentException if the key type is integral
     * @throws IllegalStateException if closed
     */
    public void forEachKey(DoubleConsumer consumer)
    {
        Objects.requireNonNull(consumer, "consumer");
        ensureOpen();
        requireFloatingKey();

        long slots = slotCount();

        for (long slot = 0; slot < slots; slot++)
        {
            long bits = keyBitsAt(slot);

            if (bits != 0)
            {
                consumer.accept(keyBitsToDouble(bits));
            }
        }

        if (containsZeroKey())
        {
            consumer.accept(0.0);
        }
    }

    private long valueAddress(long slot)
    {
        return slotAddress(slot) + valueOffset;
    }

    private long findKey(long bits)
    {
        return bits == 0 ? (containsZeroKey() ? zeroSlot() : -1) : findSlot(bits);
    }

    private long insertKey(long bits)
    {
        long slot = bits == 0 ? zeroInsert() : insertSlot(bits);

        return slot < 0 ? ~slot : slot;
    }

    private long requireSlot(long bits, long key)
    {
        long slot = findKey(bits);

        if (slot < 0)
        {
            throw new NoSuchElementException("Key not found: " + key);
        }

        return slot;
    }

    private long requireSlot(long bits, double key)
    {
        long slot = findKey(bits);

        if (slot < 0)
        {
            throw new NoSuchElementException("Key not found: " + key);
        }

        return slot;
    }

    private void writeIntegralValue(long slot, long value)
    {
        long address = valueAddress(slot);

        switch (valueKind)
        {
            case VALUE_BYTE:
                UnsafeMemory.putByte(address, (byte) value);
                return;
            case VALUE_SHORT:
                UnsafeMemory.putShort(address, (short) value);
                return;
            case VALUE_CHAR:
                UnsafeMemory.putChar(address, (char) value);
                return;
            case VALUE_INT:
                UnsafeMemory.putInt(address, (int) value);
                return;
            default:
                UnsafeMemory.putLong(address, value);
        }
    }

    private void writeFloatingValue(long slot, double value)
    {
        long address = valueAddress(slot);

        if (valueType == StructType.FLOAT)
        {
            UnsafeMemory.putFloat(address, (float) value);
            return;
        }

        UnsafeMemory.putDouble(address, value);
    }

    private long readIntegralValue(long slot)
    {
        long address = valueAddress(slot);

        switch (valueKind)
        {
            case VALUE_BYTE:
                return UnsafeMemory.getByte(address);
            case VALUE_SHORT:
                return UnsafeMemory.getShort(address);
            case VALUE_CHAR:
                return UnsafeMemory.getChar(address);
            case VALUE_INT:
                return UnsafeMemory.getInt(address);
            default:
                return UnsafeMemory.getLong(address);
        }
    }

    private double readFloatingValue(long slot)
    {
        long address = valueAddress(slot);

        if (valueType == StructType.FLOAT)
        {
            return UnsafeMemory.getFloat(address);
        }

        return UnsafeMemory.getDouble(address);
    }

    private double keyBitsToDouble(long bits)
    {
        if (keyType() == StructType.FLOAT)
        {
            return Float.intBitsToFloat((int) bits);
        }

        return Double.longBitsToDouble(bits);
    }

    private void validateIntegralValue(long value)
    {
        if (!integralValue)
        {
            throw new IllegalArgumentException("value field is " + valueType + ", not integral");
        }

        switch (valueKind)
        {
            case VALUE_BYTE:
                if (value != (byte) value)
                {
                    throw new IllegalArgumentException("value does not fit BYTE: " + value);
                }
                return;
            case VALUE_SHORT:
                if (value != (short) value)
                {
                    throw new IllegalArgumentException("value does not fit SHORT: " + value);
                }
                return;
            case VALUE_CHAR:
                if (value != (value & 0xFFFFL))
                {
                    throw new IllegalArgumentException("value does not fit CHAR: " + value);
                }
                return;
            case VALUE_INT:
                if (value != (int) value)
                {
                    throw new IllegalArgumentException("value does not fit INT: " + value);
                }
        }
    }

    private void requireIntegralValue()
    {
        if (!integralValue)
        {
            throw new IllegalArgumentException("value field is " + valueType + ", not integral");
        }
    }

    private void requireFloatingValue()
    {
        if (valueType != StructType.FLOAT && valueType != StructType.DOUBLE)
        {
            throw new IllegalArgumentException("value field is " + valueType + ", not floating");
        }
    }

    private void requireValueType(StructType expected)
    {
        if (valueType != expected)
        {
            throw new IllegalArgumentException("value field is " + valueType + ", not " + expected);
        }
    }

    private void requireIntegralKey()
    {
        if (keyType() == StructType.FLOAT || keyType() == StructType.DOUBLE)
        {
            throw new IllegalArgumentException("key field is " + keyType() + ", not integral");
        }
    }

    private void requireFloatingKey()
    {
        if (keyType() != StructType.FLOAT && keyType() != StructType.DOUBLE)
        {
            throw new IllegalArgumentException("key field is " + keyType() + ", not floating");
        }
    }

    private static int valueKindOf(StructType valueType)
    {
        switch (valueType)
        {
            case BYTE:
                return VALUE_BYTE;
            case SHORT:
                return VALUE_SHORT;
            case CHAR:
                return VALUE_CHAR;
            case INT:
                return VALUE_INT;
            case LONG:
                return VALUE_LONG;
            default:
                return -1;
        }
    }

    private static StructLayout buildLayout(StructType keyType, StructType valueType)
    {
        Objects.requireNonNull(keyType, "keyType");
        Objects.requireNonNull(valueType, "valueType");

        StructLayout.Builder builder = StructLayout.builder();

        addField(builder, keyType, "key");
        addField(builder, valueType, "value");

        return builder.build();
    }

    private static void addField(StructLayout.Builder builder, StructType type, String name)
    {
        switch (type)
        {
            case BOOLEAN:
                builder.boolField(name);
                return;
            case BYTE:
                builder.byteField(name);
                return;
            case SHORT:
                builder.shortField(name);
                return;
            case CHAR:
                builder.charField(name);
                return;
            case INT:
                builder.intField(name);
                return;
            case FLOAT:
                builder.floatField(name);
                return;
            case LONG:
                builder.longField(name);
                return;
            default:
                builder.doubleField(name);
        }
    }

    static final class Slot extends PStruct
    {
        private Slot(PStructArray<Slot> array, long index)
        {
            super(array, index);
        }
    }
}
