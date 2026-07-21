package com.tonic.jaloc.memory.internal;

import com.tonic.jaloc.memory.core.UnsafeAccess;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * A wrapper class to facilitate calling sun.misc.Unsafe methods without triggering a spam of JVM warnings.
 */
public final class UnsafeMemory
{
    private static final Object UNSAFE = UnsafeAccess.getUnsafe();

    private static final Class<?> UNSAFE_CLASS = UNSAFE.getClass();

    private static final MethodHandle ALLOCATE_MEMORY = bind("allocateMemory", long.class);

    private static final MethodHandle FREE_MEMORY = bind("freeMemory", long.class);

    private static final MethodHandle SET_MEMORY = bind("setMemory", long.class, long.class, byte.class);

    private static final MethodHandle COPY_MEMORY = bind("copyMemory", long.class, long.class, long.class);

    private static final MethodHandle GET_BYTE = bind("getByte", long.class);

    private static final MethodHandle PUT_BYTE = bind("putByte", long.class, byte.class);

    private static final MethodHandle GET_SHORT = bind("getShort", long.class);

    private static final MethodHandle PUT_SHORT = bind("putShort", long.class, short.class);

    private static final MethodHandle GET_CHAR = bind("getChar", long.class);

    private static final MethodHandle PUT_CHAR = bind("putChar", long.class, char.class);

    private static final MethodHandle GET_INT = bind("getInt", long.class);

    private static final MethodHandle PUT_INT = bind("putInt", long.class, int.class);

    private static final MethodHandle GET_LONG = bind("getLong", long.class);

    private static final MethodHandle PUT_LONG = bind("putLong", long.class, long.class);

    private static final MethodHandle GET_FLOAT = bind("getFloat", long.class);

    private static final MethodHandle PUT_FLOAT = bind("putFloat", long.class, float.class);

    private static final MethodHandle GET_DOUBLE = bind("getDouble", long.class);

    private static final MethodHandle PUT_DOUBLE = bind("putDouble", long.class, double.class);

    private UnsafeMemory()
    {
    }

    /**
     * Allocates bytes of native memory.
     *
     * @param bytes the size, must be positive
     * @return the address
     * @throws IllegalArgumentException if bytes is not positive
     */
    public static long allocate(long bytes)
    {
        if (bytes <= 0)
        {
            throw new IllegalArgumentException("bytes must be positive");
        }

        try
        {
            return (long) ALLOCATE_MEMORY.invokeExact(bytes);
        }
        catch (Throwable throwable)
        {
            throw rethrow(throwable);
        }
    }

    /**
     * Frees address; zero is a no-op.
     *
     * @param address the address to free
     */
    public static void free(long address)
    {
        if (address == 0)
        {
            return;
        }

        try
        {
            FREE_MEMORY.invokeExact(address);
        }
        catch (Throwable throwable)
        {
            throw rethrow(throwable);
        }
    }

    /**
     * Zeroes bytes at address.
     *
     * @param address the start address
     * @param bytes the byte count
     * @throws IllegalArgumentException if bytes is negative
     */
    public static void clear(long address, long bytes)
    {
        fill(address, bytes, (byte) 0);
    }

    /**
     * Fills bytes at address with value.
     *
     * @param address the start address
     * @param bytes the byte count
     * @param value the fill byte
     * @throws IllegalArgumentException if bytes is negative
     */
    public static void fill(long address, long bytes, byte value)
    {
        if (bytes < 0)
        {
            throw new IllegalArgumentException("bytes cannot be negative");
        }

        if (bytes == 0)
        {
            return;
        }

        try
        {
            SET_MEMORY.invokeExact(address, bytes, value);
        }
        catch (Throwable throwable)
        {
            throw rethrow(throwable);
        }
    }

    /**
     * Copies bytes from source to destination.
     *
     * @param source the source address
     * @param destination the destination address
     * @param bytes the byte count
     * @throws IllegalArgumentException if bytes is negative
     */
    public static void copy(long source, long destination, long bytes)
    {
        if (bytes < 0)
        {
            throw new IllegalArgumentException("bytes cannot be negative");
        }

        if (bytes == 0)
        {
            return;
        }

        try
        {
            COPY_MEMORY.invokeExact(source, destination, bytes);
        }
        catch (Throwable throwable)
        {
            throw rethrow(throwable);
        }
    }

    /**
     * Reads the byte at address.
     *
     * @param address the address
     * @return the value
     */
    public static byte getByte(long address)
    {
        try
        {
            return (byte) GET_BYTE.invokeExact(address);
        }
        catch (Throwable throwable)
        {
            throw rethrow(throwable);
        }
    }

    /**
     * Writes the byte at address.
     *
     * @param address the address
     * @param value the value to store
     */
    public static void putByte(long address, byte value)
    {
        try
        {
            PUT_BYTE.invokeExact(address, value);
        }
        catch (Throwable throwable)
        {
            throw rethrow(throwable);
        }
    }

    /**
     * Reads the short at address.
     *
     * @param address the address
     * @return the value
     */
    public static short getShort(long address)
    {
        try
        {
            return (short) GET_SHORT.invokeExact(address);
        }
        catch (Throwable throwable)
        {
            throw rethrow(throwable);
        }
    }

    /**
     * Writes the short at address.
     *
     * @param address the address
     * @param value the value to store
     */
    public static void putShort(long address, short value)
    {
        try
        {
            PUT_SHORT.invokeExact(address, value);
        }
        catch (Throwable throwable)
        {
            throw rethrow(throwable);
        }
    }

    /**
     * Reads the char at address.
     *
     * @param address the address
     * @return the value
     */
    public static char getChar(long address)
    {
        try
        {
            return (char) GET_CHAR.invokeExact(address);
        }
        catch (Throwable throwable)
        {
            throw rethrow(throwable);
        }
    }

    /**
     * Writes the char at address.
     *
     * @param address the address
     * @param value the value to store
     */
    public static void putChar(long address, char value)
    {
        try
        {
            PUT_CHAR.invokeExact(address, value);
        }
        catch (Throwable throwable)
        {
            throw rethrow(throwable);
        }
    }

    /**
     * Reads the int at address.
     *
     * @param address the address
     * @return the value
     */
    public static int getInt(long address)
    {
        try
        {
            return (int) GET_INT.invokeExact(address);
        }
        catch (Throwable throwable)
        {
            throw rethrow(throwable);
        }
    }

    /**
     * Writes the int at address.
     *
     * @param address the address
     * @param value the value to store
     */
    public static void putInt(long address, int value)
    {
        try
        {
            PUT_INT.invokeExact(address, value);
        }
        catch (Throwable throwable)
        {
            throw rethrow(throwable);
        }
    }

    /**
     * Reads the long at address.
     *
     * @param address the address
     * @return the value
     */
    public static long getLong(long address)
    {
        try
        {
            return (long) GET_LONG.invokeExact(address);
        }
        catch (Throwable throwable)
        {
            throw rethrow(throwable);
        }
    }

    /**
     * Writes the long at address.
     *
     * @param address the address
     * @param value the value to store
     */
    public static void putLong(long address, long value)
    {
        try
        {
            PUT_LONG.invokeExact(address, value);
        }
        catch (Throwable throwable)
        {
            throw rethrow(throwable);
        }
    }

    /**
     * Reads the float at address.
     *
     * @param address the address
     * @return the value
     */
    public static float getFloat(long address)
    {
        try
        {
            return (float) GET_FLOAT.invokeExact(address);
        }
        catch (Throwable throwable)
        {
            throw rethrow(throwable);
        }
    }

    /**
     * Writes the float at address.
     *
     * @param address the address
     * @param value the value to store
     */
    public static void putFloat(long address, float value)
    {
        try
        {
            PUT_FLOAT.invokeExact(address, value);
        }
        catch (Throwable throwable)
        {
            throw rethrow(throwable);
        }
    }

    /**
     * Reads the double at address.
     *
     * @param address the address
     * @return the value
     */
    public static double getDouble(long address)
    {
        try
        {
            return (double) GET_DOUBLE.invokeExact(address);
        }
        catch (Throwable throwable)
        {
            throw rethrow(throwable);
        }
    }

    /**
     * Writes the double at address.
     *
     * @param address the address
     * @param value the value to store
     */
    public static void putDouble(long address, double value)
    {
        try
        {
            PUT_DOUBLE.invokeExact(address, value);
        }
        catch (Throwable throwable)
        {
            throw rethrow(throwable);
        }
    }

    /**
     * Rounds address up to alignment.
     *
     * @param address the address
     * @param alignment a positive power of two
     * @return the aligned address
     * @throws IllegalArgumentException if alignment is invalid or the result overflows
     */
    public static long alignUp(long address, int alignment)
    {
        validateAlignment(alignment);

        long mask = alignment - 1L;

        try
        {
            return Math.addExact(address, mask) & ~mask;
        }
        catch (ArithmeticException e)
        {
            throw new IllegalArgumentException("Address alignment overflow", e);
        }
    }

    /**
     * Rejects non-power-of-two alignments.
     *
     * @param alignment the alignment to validate
     * @throws IllegalArgumentException if alignment is not a positive power of two
     */
    public static void validateAlignment(int alignment)
    {
        if (alignment <= 0 || (alignment & (alignment - 1)) != 0)
        {
            throw new IllegalArgumentException("alignment must be a positive power of two");
        }
    }

    private static MethodHandle bind(String name, Class<?>... parameterTypes)
    {
        try
        {
            Method method = UNSAFE_CLASS.getMethod(name, parameterTypes);

            return MethodHandles.lookup().unreflect(method).bindTo(UNSAFE);
        }
        catch (ReflectiveOperationException | SecurityException e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static RuntimeException rethrow(Throwable throwable)
    {
        if (throwable instanceof RuntimeException)
        {
            return (RuntimeException) throwable;
        }

        if (throwable instanceof Error)
        {
            throw (Error) throwable;
        }

        return new IllegalStateException("Unsafe operation failed", throwable);
    }
}
