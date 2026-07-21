package com.tonic.jaloc.memory.internal;

/**
 * A bounds-checked window over native memory.
 */
public final class MemoryRegion
{
    private final AllocationState state;
    private final long address;
    private final long size;

    MemoryRegion(AllocationState state, long address, long size)
    {
        this.state = state;
        this.address = address;
        this.size = size;
    }

    /**
     * @return the region size in bytes
     */
    public long size()
    {
        return size;
    }

    /**
     * @return the absolute base address
     * @throws IllegalStateException if released
     */
    public long address()
    {
        state.ensureOpen();
        return address;
    }

    /**
     * Creates a sub-region.
     *
     * @param offset the start within this region
     * @param size the sub-region size
     * @return the sub-region
     * @throws IndexOutOfBoundsException if the slice overruns this region
     * @throws IllegalStateException if released
     */
    public MemoryRegion slice(long offset, long size)
    {
        checkRange(offset, size);
        return new MemoryRegion(state, address + offset, size);
    }

    /**
     * Reads the byte at offset.
     *
     * @param offset the byte offset
     * @return the value
     * @throws IndexOutOfBoundsException if the read overruns the region
     * @throws IllegalStateException if released
     */
    public byte getByte(long offset)
    {
        checkRange(offset, Byte.BYTES);
        return UnsafeMemory.getByte(address + offset);
    }

    /**
     * Writes the byte at offset.
     *
     * @param offset the byte offset
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the write overruns the region
     * @throws IllegalStateException if released
     */
    public void putByte(long offset, byte value)
    {
        checkRange(offset, Byte.BYTES);
        UnsafeMemory.putByte(address + offset, value);
    }

    /**
     * Reads the short at offset.
     *
     * @param offset the byte offset
     * @return the value
     * @throws IndexOutOfBoundsException if the read overruns the region
     * @throws IllegalStateException if released
     */
    public short getShort(long offset)
    {
        checkRange(offset, Short.BYTES);
        return UnsafeMemory.getShort(address + offset);
    }

    /**
     * Writes the short at offset.
     *
     * @param offset the byte offset
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the write overruns the region
     * @throws IllegalStateException if released
     */
    public void putShort(long offset, short value)
    {
        checkRange(offset, Short.BYTES);
        UnsafeMemory.putShort(address + offset, value);
    }

    /**
     * Reads the char at offset.
     *
     * @param offset the byte offset
     * @return the value
     * @throws IndexOutOfBoundsException if the read overruns the region
     * @throws IllegalStateException if released
     */
    public char getChar(long offset)
    {
        return (char) getShort(offset);
    }

    /**
     * Writes the char at offset.
     *
     * @param offset the byte offset
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the write overruns the region
     * @throws IllegalStateException if released
     */
    public void putChar(long offset, char value)
    {
        putShort(offset, (short) value);
    }

    /**
     * Reads the int at offset.
     *
     * @param offset the byte offset
     * @return the value
     * @throws IndexOutOfBoundsException if the read overruns the region
     * @throws IllegalStateException if released
     */
    public int getInt(long offset)
    {
        checkRange(offset, Integer.BYTES);
        return UnsafeMemory.getInt(address + offset);
    }

    /**
     * Writes the int at offset.
     *
     * @param offset the byte offset
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the write overruns the region
     * @throws IllegalStateException if released
     */
    public void putInt(long offset, int value)
    {
        checkRange(offset, Integer.BYTES);
        UnsafeMemory.putInt(address + offset, value);
    }

    /**
     * Reads the long at offset.
     *
     * @param offset the byte offset
     * @return the value
     * @throws IndexOutOfBoundsException if the read overruns the region
     * @throws IllegalStateException if released
     */
    public long getLong(long offset)
    {
        checkRange(offset, Long.BYTES);
        return UnsafeMemory.getLong(address + offset);
    }

    /**
     * Writes the long at offset.
     *
     * @param offset the byte offset
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the write overruns the region
     * @throws IllegalStateException if released
     */
    public void putLong(long offset, long value)
    {
        checkRange(offset, Long.BYTES);
        UnsafeMemory.putLong(address + offset, value);
    }

    /**
     * Reads the float at offset.
     *
     * @param offset the byte offset
     * @return the value
     * @throws IndexOutOfBoundsException if the read overruns the region
     * @throws IllegalStateException if released
     */
    public float getFloat(long offset)
    {
        checkRange(offset, Float.BYTES);
        return UnsafeMemory.getFloat(address + offset);
    }

    /**
     * Writes the float at offset.
     *
     * @param offset the byte offset
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the write overruns the region
     * @throws IllegalStateException if released
     */
    public void putFloat(long offset, float value)
    {
        checkRange(offset, Float.BYTES);
        UnsafeMemory.putFloat(address + offset, value);
    }

    /**
     * Reads the double at offset.
     *
     * @param offset the byte offset
     * @return the value
     * @throws IndexOutOfBoundsException if the read overruns the region
     * @throws IllegalStateException if released
     */
    public double getDouble(long offset)
    {
        checkRange(offset, Double.BYTES);
        return UnsafeMemory.getDouble(address + offset);
    }

    /**
     * Writes the double at offset.
     *
     * @param offset the byte offset
     * @param value the value to store
     * @throws IndexOutOfBoundsException if the write overruns the region
     * @throws IllegalStateException if released
     */
    public void putDouble(long offset, double value)
    {
        checkRange(offset, Double.BYTES);
        UnsafeMemory.putDouble(address + offset, value);
    }

    /**
     * Copies length bytes from this region into destination.
     *
     * @param sourceOffset the start within this region
     * @param destination the target region
     * @param destinationOffset the start within destination
     * @param length the byte count
     * @throws NullPointerException if destination is null
     * @throws IndexOutOfBoundsException if either range overruns its region
     * @throws IllegalStateException if either region is released
     */
    public void copyTo(long sourceOffset, MemoryRegion destination, long destinationOffset, long length) {
        if (destination == null) {
            throw new NullPointerException("destination");
        }

        checkRange(sourceOffset, length);
        destination.checkRange(destinationOffset, length);

        UnsafeMemory.copy(
                address + sourceOffset,
                destination.address + destinationOffset,
                length
        );
    }

    /**
     * Copies length bytes from source into this region.
     *
     * @param source the source region
     * @param sourceOffset the start within source
     * @param destinationOffset the start within this region
     * @param length the byte count
     * @throws NullPointerException if source is null
     * @throws IndexOutOfBoundsException if either range overruns its region
     * @throws IllegalStateException if either region is released
     */
    public void copyFrom(MemoryRegion source, long sourceOffset, long destinationOffset, long length) {
        if (source == null) {
            throw new NullPointerException("source");
        }

        source.copyTo(sourceOffset, this, destinationOffset, length);
    }

    /**
     * Zeroes the region.
     *
     * @throws IllegalStateException if released
     */
    public void clear()
    {
        UnsafeMemory.clear(address(), size);
    }

    /**
     * Fills the region with value.
     *
     * @param value the fill byte
     * @throws IllegalStateException if released
     */
    public void fill(byte value)
    {
        UnsafeMemory.fill(address(), size, value);
    }

    private void checkRange(long offset, long length)
    {
        state.ensureOpen();

        if (offset < 0 || length < 0 || offset > size - length) {
            throw new IndexOutOfBoundsException("offset=" + offset + ", length=" + length + ", regionSize=" + size);
        }
    }
}
