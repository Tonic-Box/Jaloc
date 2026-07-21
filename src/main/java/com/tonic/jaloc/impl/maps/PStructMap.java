package com.tonic.jaloc.impl.maps;


import com.tonic.jaloc.impl.arrays.struct.*;
import com.tonic.jaloc.memory.SystemAllocator;
import com.tonic.jaloc.memory.abs.*;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A native primitive-to-struct hash map over struct entries keyed by a layout field; K is declaration metadata only, and any growth invalidates outstanding views.
 */
public final class PStructMap<K, V extends PStruct> extends AbstractNativeMap<V>
{
    private final StructViewFactory<V> viewFactory;

    /**
     * Creates an empty map keyed by the named layout field on the system allocator.
     *
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     * @param keyFieldName the key field, any type but BOOLEAN
     * @throws IllegalArgumentException if the key field is unknown or BOOLEAN
     */
    public PStructMap(StructLayout layout, StructViewFactory<V> viewFactory, String keyFieldName)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, keyFieldName, 0);
    }

    /**
     * Creates an empty map presized for expectedElements on the system allocator.
     *
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     * @param keyFieldName the key field, any type but BOOLEAN
     * @param expectedElements presizes the table
     * @throws IllegalArgumentException if the key field is unknown or BOOLEAN, or expectedElements is negative
     */
    public PStructMap(StructLayout layout, StructViewFactory<V> viewFactory, String keyFieldName, long expectedElements)
    {
        this(SystemAllocator.getInstance(), layout, viewFactory, keyFieldName, expectedElements);
    }

    /**
     * Creates an empty map presized for expectedElements on the given allocator.
     *
     * @param allocator the allocator to source memory from
     * @param layout the entry layout
     * @param viewFactory creates the entry views
     * @param keyFieldName the key field, any type but BOOLEAN
     * @param expectedElements presizes the table
     * @throws IllegalArgumentException if the key field is unknown or BOOLEAN, or expectedElements is negative
     */
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

    /**
     * Upserts key and returns its entry view; existing value fields are preserved, fresh slots arrive zeroed.
     *
     * @param key the key
     * @return the entry view
     * @throws IllegalArgumentException on key type mismatch
     * @throws IllegalStateException if closed
     */
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

    /**
     * Upserts key and returns its entry view; existing value fields are preserved, fresh slots arrive zeroed.
     *
     * @param key the key
     * @return the entry view
     * @throws IllegalArgumentException on key type mismatch
     * @throws IllegalStateException if closed
     */
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

    /**
     * Reads the entry view for key.
     *
     * @param key the key
     * @return a fresh view, or null if absent
     * @throws IllegalArgumentException on key type mismatch
     * @throws IllegalStateException if closed
     */
    public V get(long key)
    {
        ensureOpen();

        long bits = integralKeyBits(key);
        long slot = bits == 0 ? (containsZeroKey() ? zeroSlot() : -1) : findSlot(bits);

        return slot < 0 ? null : elements().at(slot);
    }

    /**
     * Reads the entry view for key.
     *
     * @param key the key
     * @return a fresh view, or null if absent
     * @throws IllegalArgumentException on key type mismatch
     * @throws IllegalStateException if closed
     */
    public V get(double key)
    {
        ensureOpen();

        long bits = floatingKeyBits(key);
        long slot = bits == 0 ? (containsZeroKey() ? zeroSlot() : -1) : findSlot(bits);

        return slot < 0 ? null : elements().at(slot);
    }

    /**
     * @return a reusable entry view for cursor lookups
     * @throws IllegalStateException if closed
     */
    public V cursor()
    {
        ensureOpen();

        return elements().cursor();
    }

    /**
     * Reads the entry for key into cursor.
     *
     * @param key the key
     * @param cursor the view to reposition
     * @return cursor, or null if absent with cursor untouched
     * @throws NullPointerException if cursor is null
     * @throws IllegalArgumentException on key type mismatch
     * @throws IllegalStateException if closed, or if cursor predates a growth
     */
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

    /**
     * Reads the entry for key into cursor.
     *
     * @param key the key
     * @param cursor the view to reposition
     * @return cursor, or null if absent with cursor untouched
     * @throws NullPointerException if cursor is null
     * @throws IllegalArgumentException on key type mismatch
     * @throws IllegalStateException if closed, or if cursor predates a growth
     */
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

        long bits = integralKeyBits(key);

        return bits == 0 ? containsZeroKey() : findSlot(bits) >= 0;
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

        long bits = floatingKeyBits(key);

        return bits == 0 ? containsZeroKey() : findSlot(bits) >= 0;
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
     * Emits every entry through one reusable view; do not hold it across calls.
     *
     * @param consumer the receiver
     * @throws NullPointerException if consumer is null
     * @throws IllegalStateException if closed
     */
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
