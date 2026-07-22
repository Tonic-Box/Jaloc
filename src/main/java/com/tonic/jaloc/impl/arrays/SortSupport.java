package com.tonic.jaloc.impl.arrays;

final class SortSupport
{
    private SortSupport()
    {
    }

    static boolean trivialPass(long[] counts, int bucketBase, long length)
    {
        for (int bucket = 0; bucket < 256; bucket++)
        {
            if (counts[bucketBase + bucket] == length)
            {
                return true;
            }
        }

        return false;
    }
}
