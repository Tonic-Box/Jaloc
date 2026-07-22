package com.tonic.jaloc.memory;

import com.tonic.jaloc.memory.iface.AllocationOwner;
import com.tonic.jaloc.memory.iface.NativeAllocator;
import com.tonic.jaloc.memory.internal.AllocationRecord;
import com.tonic.jaloc.memory.internal.AllocationState;
import com.tonic.jaloc.memory.internal.MappedExtent;
import com.tonic.jaloc.memory.internal.MappedFiles;
import com.tonic.jaloc.memory.internal.MemoryBlock;
import com.tonic.jaloc.memory.internal.NativeCleaner;
import com.tonic.jaloc.memory.internal.UnsafeMemory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An allocator backing each block with a memory-mapped temp file the kernel deletes at process exit, so pages are
 * OS-reclaimable under memory pressure instead of pinned like malloc. Blocks use disk transiently while live.
 * Accessing a mapped page whose backing file hits an I/O error (disk full, device loss) can terminate the JVM.
 */
public final class MappedAllocator implements NativeAllocator, AllocationOwner, AutoCloseable
{
    private static final AtomicLong FILE_IDS = new AtomicLong();

    private final Path directory;
    private final ConcurrentHashMap<Long, Mapping> mappings = new ConcurrentHashMap<>();
    private final AtomicLong mappedBytes = new AtomicLong();

    private volatile boolean open = true;

    private MappedAllocator(Path directory)
    {
        this.directory = directory;
    }

    /**
     * Creates a spill allocator over temp files in the system temp directory.
     *
     * @return the allocator
     * @throws UnsupportedOperationException if file mapping is unavailable on this JVM
     */
    public static MappedAllocator ephemeral()
    {
        return ephemeral(Paths.get(System.getProperty("java.io.tmpdir")));
    }

    /**
     * Creates a spill allocator over temp files in directory.
     *
     * @param directory the directory to place backing files in
     * @return the allocator
     * @throws UnsupportedOperationException if file mapping is unavailable on this JVM
     */
    public static MappedAllocator ephemeral(Path directory)
    {
        Objects.requireNonNull(directory, "directory");

        if (!MappedFiles.available())
        {
            throw new UnsupportedOperationException("File mapping requires a 64-bit JVM");
        }

        return new MappedAllocator(directory);
    }

    @Override
    public boolean clearRequired()
    {
        return false;
    }

    @Override
    public MemoryBlock allocate(long bytes, int alignment)
    {
        ensureOpen();

        if (bytes < 0)
        {
            throw new IllegalArgumentException("bytes cannot be negative");
        }

        UnsafeMemory.validateAlignment(alignment);

        if (bytes == 0)
        {
            return new MemoryBlock(new AllocationState(new AllocationRecord(this, 0, 0, 0, alignment), 0));
        }

        long padding = alignment - 1L;
        long reservedBytes = Math.addExact(bytes, padding);

        FileChannel channel = openBackingFile();
        boolean successful = false;

        try
        {
            MappedFiles.extend(channel, reservedBytes);

            MappedExtent extent = MappedFiles.map(channel, 0, reservedBytes);

            long rawAddress = extent.address();
            long alignedAddress = UnsafeMemory.alignUp(rawAddress, alignment);

            AllocationRecord record = new AllocationRecord(this, rawAddress, bytes, reservedBytes, alignment);
            AllocationState state = new AllocationState(record, alignedAddress);

            mappings.put(rawAddress, new Mapping(extent, channel));
            mappedBytes.addAndGet(reservedBytes);

            NativeCleaner.register(state, record);

            successful = true;
            return new MemoryBlock(state);
        }
        finally
        {
            if (!successful)
            {
                closeQuietly(channel);
            }
        }
    }

    @Override
    public void release(AllocationRecord allocation)
    {
        Mapping mapping = mappings.remove(allocation.rawAddress());

        if (mapping == null)
        {
            return;
        }

        mappedBytes.addAndGet(-mapping.extent.length());
        MappedFiles.unmap(mapping.extent);
        closeQuietly(mapping.channel);
    }

    @Override
    public boolean isOpen()
    {
        return open;
    }

    /**
     * @return the total bytes currently mapped by live blocks
     */
    public long mappedBytes()
    {
        return mappedBytes.get();
    }

    /**
     * Refuses further allocations. Outstanding blocks stay mapped until they close or the cleaner reclaims them;
     * the kernel deletes every backing file at process exit regardless. Safe to call more than once.
     */
    @Override
    public void close()
    {
        open = false;
    }

    private void ensureOpen()
    {
        if (!open)
        {
            throw new IllegalStateException("Allocator has been closed");
        }
    }

    private FileChannel openBackingFile()
    {
        while (true)
        {
            Path path = directory.resolve("jaloc-" + Long.toHexString(FILE_IDS.getAndIncrement()) + "-" + Long.toHexString(System.nanoTime()) + ".spill");

            try
            {
                return FileChannel.open(
                        path,
                        StandardOpenOption.CREATE_NEW,
                        StandardOpenOption.READ,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.SPARSE,
                        StandardOpenOption.DELETE_ON_CLOSE
                );
            }
            catch (FileAlreadyExistsException ignored)
            {
            }
            catch (IOException e)
            {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static void closeQuietly(FileChannel channel)
    {
        try
        {
            channel.close();
        }
        catch (IOException ignored)
        {
        }
    }

    private static final class Mapping
    {
        private final MappedExtent extent;
        private final FileChannel channel;

        private Mapping(MappedExtent extent, FileChannel channel)
        {
            this.extent = extent;
            this.channel = channel;
        }
    }
}
