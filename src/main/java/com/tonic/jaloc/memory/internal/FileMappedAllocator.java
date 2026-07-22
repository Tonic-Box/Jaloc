package com.tonic.jaloc.memory.internal;

import com.tonic.jaloc.memory.iface.AllocationOwner;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * A single-allocation allocator mapping one named file, for collections whose contents outlive the process.
 * Fresh files are zero-filled by the OS and reopened files keep their bytes, so blocks are never cleared.
 */
public final class FileMappedAllocator implements NativeAllocator, AllocationOwner, AutoCloseable
{
    private final Path path;
    private final long fileBytes;
    private final boolean creating;

    private FileChannel channel;
    private MappedExtent extent;
    private boolean allocated;
    private boolean open = true;

    private FileMappedAllocator(Path path, long fileBytes, boolean creating)
    {
        this.path = path;
        this.fileBytes = fileBytes;
        this.creating = creating;
    }

    /**
     * Prepares an allocator over a new file of byteSize bytes.
     *
     * @param path the file to create
     * @param byteSize the file size in bytes
     * @return the allocator
     * @throws IllegalArgumentException if byteSize is not positive
     * @throws UnsupportedOperationException if file mapping is unavailable on this JVM
     */
    public static FileMappedAllocator create(Path path, long byteSize)
    {
        Objects.requireNonNull(path, "path");
        requireMapping();

        if (byteSize <= 0)
        {
            throw new IllegalArgumentException("byteSize must be positive");
        }

        return new FileMappedAllocator(path, byteSize, true);
    }

    /**
     * Prepares an allocator over an existing file, mapping its full length.
     *
     * @param path the file to open
     * @return the allocator
     * @throws IllegalArgumentException if the file is empty
     * @throws UncheckedIOException if the file cannot be read
     * @throws UnsupportedOperationException if file mapping is unavailable on this JVM
     */
    public static FileMappedAllocator open(Path path)
    {
        Objects.requireNonNull(path, "path");
        requireMapping();

        long size;

        try
        {
            size = Files.size(path);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }

        if (size <= 0)
        {
            throw new IllegalArgumentException("file is empty: " + path);
        }

        return new FileMappedAllocator(path, size, false);
    }

    /**
     * @return the byte length of the mapped file
     */
    public long fileBytes()
    {
        return fileBytes;
    }

    @Override
    public boolean clearRequired()
    {
        return false;
    }

    @Override
    public MemoryBlock allocate(long bytes, int alignment)
    {
        if (!open)
        {
            throw new IllegalStateException("Allocator has been closed");
        }

        if (allocated)
        {
            throw new IllegalStateException("File allocators serve exactly one allocation");
        }

        UnsafeMemory.validateAlignment(alignment);

        if (bytes <= 0 || bytes > fileBytes)
        {
            throw new IllegalArgumentException("bytes must be positive and no larger than the file");
        }

        FileChannel opened = openChannel();
        boolean successful = false;

        try
        {
            if (creating)
            {
                MappedFiles.extend(opened, fileBytes);
            }

            MappedExtent mapped = MappedFiles.map(opened, 0, fileBytes);
            long rawAddress = mapped.address();

            if (UnsafeMemory.alignUp(rawAddress, alignment) != rawAddress)
            {
                MappedFiles.unmap(mapped);
                throw new IllegalArgumentException("alignment exceeds the page alignment of a mapping");
            }

            AllocationRecord record = new AllocationRecord(this, rawAddress, bytes, fileBytes, alignment);
            AllocationState state = new AllocationState(record, rawAddress);

            channel = opened;
            extent = mapped;
            allocated = true;

            NativeCleaner.register(state, record);

            successful = true;
            return new MemoryBlock(state);
        }
        finally
        {
            if (!successful)
            {
                closeQuietly(opened);
            }
        }
    }

    @Override
    public void release(AllocationRecord allocation)
    {
        if (extent == null)
        {
            return;
        }

        MappedExtent released = extent;
        FileChannel owned = channel;

        extent = null;
        channel = null;
        open = false;

        MappedFiles.unmap(released);

        try
        {
            owned.force(true);
        }
        catch (IOException ignored)
        {
        }

        closeQuietly(owned);
    }

    @Override
    public boolean isOpen()
    {
        return open;
    }

    /**
     * Releases the mapping and closes the file if an allocation is still live. Safe to call more than once.
     */
    @Override
    public void close()
    {
        if (extent != null)
        {
            release(null);
            return;
        }

        open = false;
    }

    private FileChannel openChannel()
    {
        try
        {
            if (creating)
            {
                return FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.READ, StandardOpenOption.WRITE);
            }

            return FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    private static void requireMapping()
    {
        if (!MappedFiles.available())
        {
            throw new UnsupportedOperationException("File mapping requires a 64-bit JVM");
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
}
