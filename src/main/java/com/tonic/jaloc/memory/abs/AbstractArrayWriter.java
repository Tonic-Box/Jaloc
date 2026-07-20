package com.tonic.jaloc.memory.abs;

public abstract class AbstractArrayWriter
{
    private final long capacity;
    private long position;

    protected AbstractArrayWriter(long capacity)
    {
        this.capacity = capacity;
    }

    public final long position()
    {
        return position;
    }

    public final long remaining()
    {
        return capacity - position;
    }

    public final boolean hasRemaining()
    {
        return position < capacity;
    }

    public final void position(long position)
    {
        if (position < 0 || position > capacity) {
            throw new IndexOutOfBoundsException("position=" + position + ", capacity=" + capacity);
        }

        this.position = position;
    }

    public final void reset()
    {
        position = 0;
    }

    protected final long nextIndex()
    {
        if (position >= capacity) {
            throw new IndexOutOfBoundsException("Writer has no remaining capacity");
        }

        return position++;
    }
}