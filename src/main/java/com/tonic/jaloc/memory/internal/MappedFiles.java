package com.tonic.jaloc.memory.internal;

import com.tonic.jaloc.memory.core.UnsafeAccess;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Objects;

/**
 * Maps files into native memory through the JDK's own map0/unmap0 natives, falling back to the public API below 2GB; -Djaloc.mapped.fallback=true forces the fallback.
 */
public final class MappedFiles
{
    private static final int STRATEGY_NONE = 0;
    private static final int STRATEGY_CHANNEL = 1;
    private static final int STRATEGY_CHANNEL_SYNC = 2;
    private static final int STRATEGY_DISPATCHER = 3;
    private static final int STRATEGY_DISPATCHER_STATIC = 4;

    private static final String CHANNEL_CLASS = "sun.nio.ch.FileChannelImpl";

    private static final String[] DISPATCHER_CLASSES = { "sun.nio.ch.FileDispatcherImpl", "sun.nio.ch.UnixFileDispatcherImpl" };

    private static final Object UNSAFE = UnsafeAccess.getUnsafe();

    private static final Class<?> UNSAFE_CLASS = UNSAFE.getClass();

    private static final MethodHandle OBJECT_FIELD_OFFSET = bind("objectFieldOffset", Field.class);

    private static final MethodHandle STATIC_FIELD_BASE = bind("staticFieldBase", Field.class);

    private static final MethodHandle STATIC_FIELD_OFFSET = bind("staticFieldOffset", Field.class);

    private static final MethodHandle GET_OBJECT_FIELD = bind("getObject", Object.class, long.class);

    private static final MethodHandle GET_LONG_FIELD = bind("getLong", Object.class, long.class);

    private static final MethodHandle GET_INT_FIELD = bind("getInt", Object.class, long.class);

    private static final boolean BIT_64 = detect64Bit();

    private static final int STRATEGY;

    private static final MethodHandle MAP0;

    private static final MethodHandle UNMAP0;

    private static final int PROT_RW;

    private static final long ND_OFFSET;

    private static final long FD_OFFSET;

    private static final long ADDRESS_OFFSET;

    private static final MethodHandle INVOKE_CLEANER;

    private static final Method CLEANER_METHOD;

    private static final Method CLEAN_METHOD;

    static
    {
        MethodHandle map0 = null;
        MethodHandle unmap0 = null;
        int strategy = STRATEGY_NONE;
        int prot = 1;
        long ndOffset = 0;
        long fdOffset = 0;

        if (BIT_64 && !Boolean.getBoolean("jaloc.mapped.fallback"))
        {
            MethodHandles.Lookup lookup = acquireLookup();

            if (lookup != null)
            {
                MethodType plain = MethodType.methodType(long.class, int.class, long.class, long.class);
                MethodType sync = MethodType.methodType(long.class, int.class, long.class, long.class, boolean.class);
                MethodType dispatched = MethodType.methodType(long.class, FileDescriptor.class, int.class, long.class, long.class, boolean.class);

                prot = readProt(CHANNEL_CLASS);
                unmap0 = probeUnmap(lookup, CHANNEL_CLASS);

                map0 = findVirtual(lookup, CHANNEL_CLASS, "map0", plain);

                if (map0 != null)
                {
                    strategy = STRATEGY_CHANNEL;
                }
                else
                {
                    map0 = findVirtual(lookup, CHANNEL_CLASS, "map0", sync);

                    if (map0 != null)
                    {
                        strategy = STRATEGY_CHANNEL_SYNC;
                    }
                }

                for (String name : DISPATCHER_CLASSES)
                {
                    if (map0 != null)
                    {
                        break;
                    }

                    map0 = findVirtual(lookup, name, "map0", dispatched);
                    strategy = STRATEGY_DISPATCHER;

                    if (map0 == null)
                    {
                        map0 = findStatic(lookup, name, "map0", dispatched);
                        strategy = STRATEGY_DISPATCHER_STATIC;
                    }

                    if (map0 == null)
                    {
                        strategy = STRATEGY_NONE;
                        continue;
                    }

                    if (unmap0 == null)
                    {
                        unmap0 = probeUnmap(lookup, name);
                    }

                    ndOffset = fieldOffset(CHANNEL_CLASS, "nd");
                    fdOffset = fieldOffset(CHANNEL_CLASS, "fd");

                    if (fdOffset < 0 || (strategy == STRATEGY_DISPATCHER && ndOffset < 0))
                    {
                        strategy = STRATEGY_NONE;
                        map0 = null;
                    }
                }
            }
        }

        if (map0 == null || unmap0 == null)
        {
            strategy = STRATEGY_NONE;
            map0 = null;
            unmap0 = null;
        }

        STRATEGY = strategy;
        MAP0 = map0;
        UNMAP0 = unmap0;
        PROT_RW = prot;
        ND_OFFSET = ndOffset;
        FD_OFFSET = fdOffset;

        long addressOffset = -1;

        try
        {
            addressOffset = (long) OBJECT_FIELD_OFFSET.invoke(Buffer.class.getDeclaredField("address"));
        }
        catch (Throwable ignored)
        {
        }

        ADDRESS_OFFSET = addressOffset;

        MethodHandle invokeCleaner = null;

        try
        {
            Method method = UNSAFE_CLASS.getMethod("invokeCleaner", ByteBuffer.class);
            invokeCleaner = MethodHandles.lookup().unreflect(method).bindTo(UNSAFE);
        }
        catch (Throwable ignored)
        {
        }

        INVOKE_CLEANER = invokeCleaner;

        Method cleanerMethod = null;
        Method cleanMethod = null;

        if (invokeCleaner == null)
        {
            try
            {
                Class<?> directBuffer = Class.forName("sun.nio.ch.DirectBuffer");
                cleanerMethod = directBuffer.getMethod("cleaner");
                cleanMethod = cleanerMethod.getReturnType().getMethod("clean");
            }
            catch (Throwable ignored)
            {
            }
        }

        CLEANER_METHOD = cleanerMethod;
        CLEAN_METHOD = cleanMethod;
    }

    private MappedFiles()
    {
    }

    /**
     * @return true if file mapping works on this JVM
     */
    public static boolean available()
    {
        return BIT_64 && (STRATEGY != STRATEGY_NONE || ADDRESS_OFFSET >= 0);
    }

    /**
     * @return true if the JDK's map0/unmap0 natives are bound; false means the 2GB-capped public fallback
     */
    public static boolean map0Bound()
    {
        return STRATEGY != STRATEGY_NONE;
    }

    /**
     * Maps length bytes of channel's file starting at position; position must be a multiple of the OS allocation granularity.
     *
     * @param channel the open read-write channel
     * @param position the file offset
     * @param length the byte count
     * @return the live mapping
     * @throws IllegalArgumentException if position is negative or length is not positive
     * @throws UnsupportedOperationException if only the fallback is bound and length exceeds 2GB-1
     * @throws UncheckedIOException if mapping fails
     */
    public static MappedExtent map(FileChannel channel, long position, long length)
    {
        Objects.requireNonNull(channel, "channel");

        if (position < 0)
        {
            throw new IllegalArgumentException("position cannot be negative");
        }

        if (length <= 0)
        {
            throw new IllegalArgumentException("length must be positive");
        }

        if (STRATEGY != STRATEGY_NONE)
        {
            return new MappedExtent(mapNative(channel, position, length), length, null);
        }

        return mapFallback(channel, position, length);
    }

    /**
     * Releases a mapping.
     *
     * @param extent the mapping to release
     */
    public static void unmap(MappedExtent extent)
    {
        Objects.requireNonNull(extent, "extent");

        ByteBuffer buffer = extent.buffer();

        try
        {
            if (buffer == null)
            {
                UNMAP0.invoke(extent.address(), extent.length());
            }
            else if (INVOKE_CLEANER != null)
            {
                INVOKE_CLEANER.invoke(buffer);
            }
            else if (CLEAN_METHOD != null)
            {
                CLEAN_METHOD.invoke(CLEANER_METHOD.invoke(buffer));
            }
        }
        catch (RuntimeException | Error e)
        {
            throw e;
        }
        catch (Throwable throwable)
        {
            throw new IllegalStateException("File unmapping failed", throwable);
        }
    }

    /**
     * Grows channel's file to newLength if shorter; a no-op otherwise.
     *
     * @param channel the open read-write channel
     * @param newLength the target length
     * @throws IllegalArgumentException if newLength is not positive
     * @throws UncheckedIOException if the write fails
     */
    public static void extend(FileChannel channel, long newLength)
    {
        Objects.requireNonNull(channel, "channel");

        if (newLength <= 0)
        {
            throw new IllegalArgumentException("newLength must be positive");
        }

        try
        {
            if (channel.size() >= newLength)
            {
                return;
            }

            channel.write(ByteBuffer.wrap(new byte[1]), newLength - 1);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    private static long mapNative(FileChannel channel, long position, long length)
    {
        try
        {
            return invokeMap(channel, position, length);
        }
        catch (OutOfMemoryError firstAttempt)
        {
            System.gc();

            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }

            return invokeMap(channel, position, length);
        }
    }

    private static long invokeMap(FileChannel channel, long position, long length)
    {
        try
        {
            switch (STRATEGY)
            {
                case STRATEGY_CHANNEL:
                    return (long) MAP0.invoke(channel, PROT_RW, position, length);
                case STRATEGY_CHANNEL_SYNC:
                    return (long) MAP0.invoke(channel, PROT_RW, position, length, false);
                case STRATEGY_DISPATCHER:
                    return (long) MAP0.invoke(GET_OBJECT_FIELD.invoke(channel, ND_OFFSET), GET_OBJECT_FIELD.invoke(channel, FD_OFFSET), PROT_RW, position, length, false);
                default:
                    return (long) MAP0.invoke(GET_OBJECT_FIELD.invoke(channel, FD_OFFSET), PROT_RW, position, length, false);
            }
        }
        catch (RuntimeException | Error e)
        {
            throw e;
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
        catch (Throwable throwable)
        {
            throw new IllegalStateException("File mapping failed", throwable);
        }
    }

    private static MappedExtent mapFallback(FileChannel channel, long position, long length)
    {
        if (ADDRESS_OFFSET < 0)
        {
            throw new UnsupportedOperationException("File mapping is unavailable on this JVM");
        }

        if (length > Integer.MAX_VALUE)
        {
            throw new UnsupportedOperationException("Mappings beyond 2GB-1 bytes require map0 access, which is unavailable on this JVM");
        }

        try
        {
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, position, length);
            long address = (long) GET_LONG_FIELD.invoke(buffer, ADDRESS_OFFSET);
            return new MappedExtent(address, length, buffer);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
        catch (RuntimeException | Error e)
        {
            throw e;
        }
        catch (Throwable throwable)
        {
            throw new IllegalStateException("File mapping failed", throwable);
        }
    }

    private static MethodHandles.Lookup acquireLookup()
    {
        Field field;

        try
        {
            field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        }
        catch (Throwable ignored)
        {
            return null;
        }

        try
        {
            Object base = STATIC_FIELD_BASE.invoke(field);
            long offset = (long) STATIC_FIELD_OFFSET.invoke(field);
            return (MethodHandles.Lookup) GET_OBJECT_FIELD.invoke(base, offset);
        }
        catch (Throwable ignored)
        {
        }

        try
        {
            field.setAccessible(true);
            return (MethodHandles.Lookup) field.get(null);
        }
        catch (Throwable ignored)
        {
            return null;
        }
    }

    private static int readProt(String className)
    {
        try
        {
            Field field = Class.forName(className).getDeclaredField("MAP_RW");
            Object base = STATIC_FIELD_BASE.invoke(field);
            long offset = (long) STATIC_FIELD_OFFSET.invoke(field);
            return (int) GET_INT_FIELD.invoke(base, offset);
        }
        catch (Throwable ignored)
        {
            return 1;
        }
    }

    private static MethodHandle probeUnmap(MethodHandles.Lookup lookup, String className)
    {
        MethodHandle handle = findStatic(lookup, className, "unmap0", MethodType.methodType(int.class, long.class, long.class));

        if (handle != null)
        {
            return handle;
        }

        return findStatic(lookup, className, "unmap0", MethodType.methodType(void.class, long.class, long.class));
    }

    private static MethodHandle findVirtual(MethodHandles.Lookup lookup, String className, String name, MethodType type)
    {
        try
        {
            return lookup.findVirtual(Class.forName(className), name, type);
        }
        catch (Throwable ignored)
        {
            return null;
        }
    }

    private static MethodHandle findStatic(MethodHandles.Lookup lookup, String className, String name, MethodType type)
    {
        try
        {
            return lookup.findStatic(Class.forName(className), name, type);
        }
        catch (Throwable ignored)
        {
            return null;
        }
    }

    private static long fieldOffset(String className, String name)
    {
        try
        {
            return (long) OBJECT_FIELD_OFFSET.invoke(Class.forName(className).getDeclaredField(name));
        }
        catch (Throwable ignored)
        {
            return -1;
        }
    }

    private static boolean detect64Bit()
    {
        String model = System.getProperty("sun.arch.data.model");

        if (model != null)
        {
            return model.contains("64");
        }

        return System.getProperty("os.arch", "").contains("64");
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
}
