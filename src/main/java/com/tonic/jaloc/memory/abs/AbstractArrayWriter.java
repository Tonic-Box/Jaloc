package com.tonic.jaloc.memory.abs;

/**
 * Positional state for forward-only array writers.
 */
public abstract class AbstractArrayWriter
{
    private final long capacity;
    private long position;

    protected AbstractArrayWriter(long capacity)
    {
        this.capacity = capacity;
    }

    /**
     * @return the current position
     */
    public final long position()
    {
        return position;
    }

    /**
     * @return elements left before capacity
     */
    public final long remaining()
    {
        return capacity - position;
    }

    /**
     * @return true if capacity remains
     */
    public final boolean hasRemaining()
    {
        return position < capacity;
    }

    /**
     * Moves to position.
     *
     * @param position the new position
     * @throws IndexOutOfBoundsException if position is out of range
     */
    public final void position(long position)
    {
        if (position < 0 || position > capacity) {
            throw new IndexOutOfBoundsException("position=" + position + ", capacity=" + capacity);
        }

        this.position = position;
    }

    /**
     * Moves to position zero.
     */
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
