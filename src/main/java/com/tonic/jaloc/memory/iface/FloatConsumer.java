package com.tonic.jaloc.memory.iface;

/**
 * A float-accepting consumer; java.util.function has no float variant.
 */
@FunctionalInterface
public interface FloatConsumer
{
    /**
     * @param value the value to accept
     */
    void accept(float value);
}
