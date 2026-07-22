package com.tonic.jaloc.memory.abs;

import com.tonic.jaloc.impl.arrays.struct.PStruct;
import com.tonic.jaloc.impl.arrays.struct.PStructArray;
import com.tonic.jaloc.impl.arrays.struct.PStructWriter;
import com.tonic.jaloc.impl.arrays.struct.StructViewFactory;
import com.tonic.jaloc.memory.data.struct.StructLayout;
import com.tonic.jaloc.memory.iface.NativeAllocator;

import java.util.Objects;

/**
 * The ring-storage base for struct collections: layout, view factory, and array creation.
 */
public abstract class AbstractStructRing<T extends PStruct> extends AbstractNativeRing<PStructArray<T>, PStructWriter<T>>
{
    private final StructLayout layout;
    private final StructViewFactory<T> viewFactory;

    protected AbstractStructRing(NativeAllocator allocator, StructLayout layout, StructViewFactory<T> viewFactory, long capacity)
    {
        super(Objects.requireNonNull(allocator, "allocator"), new PStructArray<>(allocator, viewFactory, layout, capacity));

        this.layout = layout;
        this.viewFactory = viewFactory;
    }

    /**
     * @return the entry layout
     */
    public final StructLayout layout()
    {
        return layout;
    }

    @Override
    protected final PStructArray<T> createArray(NativeAllocator allocator, long capacity)
    {
        return new PStructArray<>(allocator, viewFactory, layout, capacity);
    }
}
