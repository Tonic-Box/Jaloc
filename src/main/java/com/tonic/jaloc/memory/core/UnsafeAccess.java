package com.tonic.jaloc.memory.core;

import java.lang.reflect.Field;

public final class UnsafeAccess
{
    private static final Object UNSAFE = acquireUnsafe();

    private UnsafeAccess()
    {
    }

    private static Object acquireUnsafe()
    {
        try
        {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return field.get(null);
        }
        catch (ReflectiveOperationException | SecurityException e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Object getUnsafe()
    {
        return UNSAFE;
    }
}