package com.tonic.jaloc.memory.internal;

public final class MemoryRegion
{
    private final AllocationState state;
    private final long offset;
    private final long size;

    MemoryRegion(AllocationState state, long offset, long size)
    {
        this.state = state;
        this.offset = offset;
        this.size = size;
    }

    public long size()
    {
        return size;
    }

    public long address()
    {
        state.ensureOpen();
        return state.address() + offset;
    }

    public MemoryRegion slice(long offset, long size)
    {
        checkRange(offset, size);
        return new MemoryRegion(state, this.offset + offset, size);
    }

    public byte getByte(long offset)
    {
        checkRange(offset, Byte.BYTES);
        return UnsafeMemory.getByte(address() + offset);
    }

    public void putByte(long offset, byte value)
    {
        checkRange(offset, Byte.BYTES);
        UnsafeMemory.putByte(address() + offset, value);
    }

    public short getShort(long offset)
    {
        checkRange(offset, Short.BYTES);
        return UnsafeMemory.getShort(address() + offset);
    }

    public void putShort(long offset, short value)
    {
        checkRange(offset, Short.BYTES);
        UnsafeMemory.putShort(address() + offset, value);
    }

    public char getChar(long offset)
    {
        return (char) getShort(offset);
    }

    public void putChar(long offset, char value)
    {
        putShort(offset, (short) value);
    }

    public int getInt(long offset)
    {
        checkRange(offset, Integer.BYTES);
        return UnsafeMemory.getInt(address() + offset);
    }

    public void putInt(long offset, int value)
    {
        checkRange(offset, Integer.BYTES);
        UnsafeMemory.putInt(address() + offset, value);
    }

    public long getLong(long offset)
    {
        checkRange(offset, Long.BYTES);
        return UnsafeMemory.getLong(address() + offset);
    }

    public void putLong(long offset, long value)
    {
        checkRange(offset, Long.BYTES);
        UnsafeMemory.putLong(address() + offset, value);
    }

    public float getFloat(long offset)
    {
        checkRange(offset, Float.BYTES);
        return UnsafeMemory.getFloat(address() + offset);
    }

    public void putFloat(long offset, float value)
    {
        checkRange(offset, Float.BYTES);
        UnsafeMemory.putFloat(address() + offset, value);
    }

    public double getDouble(long offset)
    {
        checkRange(offset, Double.BYTES);
        return UnsafeMemory.getDouble(address() + offset);
    }

    public void putDouble(long offset, double value)
    {
        checkRange(offset, Double.BYTES);
        UnsafeMemory.putDouble(address() + offset, value);
    }

    public void copyTo(
            long sourceOffset,
            MemoryRegion destination,
            long destinationOffset,
            long length
    ) {
        if (destination == null) {
            throw new NullPointerException("destination");
        }

        checkRange(sourceOffset, length);
        destination.checkRange(destinationOffset, length);

        UnsafeMemory.copy(
                address() + sourceOffset,
                destination.address() + destinationOffset,
                length
        );
    }

    public void copyFrom(MemoryRegion source, long sourceOffset, long destinationOffset, long length) {
        if (source == null) {
            throw new NullPointerException("source");
        }

        source.copyTo(sourceOffset, this, destinationOffset, length);
    }

    public void clear()
    {
        UnsafeMemory.clear(address(), size);
    }

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