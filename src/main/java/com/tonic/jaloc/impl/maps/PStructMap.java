package com.tonic.jaloc.impl.maps;


import com.tonic.jaloc.impl.arrays.struct.*;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.*;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.Objects;
import java.util.function.Consumer;

public final class PStructMap<K, V extends PStruct> extends AbstractNativeMap<V>
{
    private final StructViewFactory<V> viewFactory;

    public PStructMap(StructLayout layout, StructViewFactory<V> viewFactory, String keyFieldName)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, keyFieldName, 0);
    }

    public PStructMap(StructLayout layout, StructViewFactory<V> viewFactory, String keyFieldName, long expectedElements)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, keyFieldName, expectedElements);
    }

    public PStructMap(NativeAllocator allocator, StructLayout layout, StructViewFactory<V> viewFactory, String keyFieldName, long expectedElements)
    {
        super(Objects.requireNonNull(allocator, "allocator"), new PStructArray<>(allocator, viewFactory, layout, tableLength(expectedElements)), keyFieldName);

        this.viewFactory = viewFactory;
    }

    @Override
    protected PStructArray<V> createArray(NativeAllocator allocator, long capacity)
    {
        return new PStructArray<>(allocator, viewFactory, layout(), capacity);
    }

    public V put(long key)
    {
        ensureOpen();

        long bits = integralKeyBits(key);
        long slot = bits == 0 ? zeroInsert() : insertSlot(bits);

        if (slot < 0)
        {
            slot = ~slot;
        }

        return elements().at(slot);
    }

    public V put(double key)
    {
        ensureOpen();

        long bits = floatingKeyBits(key);
        long slot = bits == 0 ? zeroInsert() : insertSlot(bits);

        if (slot < 0)
        {
            slot = ~slot;
        }

        return elements().at(slot);
    }

    public V get(long key)
    {
        ensureOpen();

        long bits = integralKeyBits(key);
        long slot = bits == 0 ? (containsZeroKey() ? zeroSlot() : -1) : findSlot(bits);

        return slot < 0 ? null : elements().at(slot);
    }

    public V get(double key)
    {
        ensureOpen();

        long bits = floatingKeyBits(key);
        long slot = bits == 0 ? (containsZeroKey() ? zeroSlot() : -1) : findSlot(bits);

        return slot < 0 ? null : elements().at(slot);
    }

    public V cursor()
    {
        ensureOpen();

        return elements().cursor();
    }

    public V get(long key, V cursor)
    {
        Objects.requireNonNull(cursor, "cursor");
        ensureOpen();

        long bits = integralKeyBits(key);
        long slot = bits == 0 ? (containsZeroKey() ? zeroSlot() : -1) : findSlot(bits);

        if (slot < 0)
        {
            return null;
        }

        cursor.moveTo(slot);
        return cursor;
    }

    public V get(double key, V cursor)
    {
        Objects.requireNonNull(cursor, "cursor");
        ensureOpen();

        long bits = floatingKeyBits(key);
        long slot = bits == 0 ? (containsZeroKey() ? zeroSlot() : -1) : findSlot(bits);

        if (slot < 0)
        {
            return null;
        }

        cursor.moveTo(slot);
        return cursor;
    }

    public boolean containsKey(long key)
    {
        ensureOpen();

        long bits = integralKeyBits(key);

        return bits == 0 ? containsZeroKey() : findSlot(bits) >= 0;
    }

    public boolean containsKey(double key)
    {
        ensureOpen();

        long bits = floatingKeyBits(key);

        return bits == 0 ? containsZeroKey() : findSlot(bits) >= 0;
    }

    public boolean remove(long key)
    {
        ensureOpen();

        long bits = integralKeyBits(key);

        return bits == 0 ? removeZero() : removeSlot(bits);
    }

    public boolean remove(double key)
    {
        ensureOpen();

        long bits = floatingKeyBits(key);

        return bits == 0 ? removeZero() : removeSlot(bits);
    }

    public void forEach(Consumer<? super V> consumer)
    {
        Objects.requireNonNull(consumer, "consumer");
        ensureOpen();

        V cursor = elements().cursor();
        long slots = slotCount();

        for (long slot = 0; slot < slots; slot++)
        {
            if (keyBitsAt(slot) != 0)
            {
                cursor.moveTo(slot);
                consumer.accept(cursor);
            }
        }

        if (containsZeroKey())
        {
            cursor.moveTo(zeroSlot());
            consumer.accept(cursor);
        }
    }
}
