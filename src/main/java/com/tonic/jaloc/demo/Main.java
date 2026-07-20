package com.tonic.jaloc.demo;

import com.tonic.jaloc.impl.arrays.struct.PStructArray;
import com.tonic.jaloc.impl.arrays.struct.PStructWriter;

public class Main {
    public static void main(String[] args) {
        try(PStructArray<DateStruct> dates = new PStructArray<>(DateStruct::new, DateStruct.LAYOUT, 10))
        {
            PStructWriter<DateStruct> writer = dates.writer();

            writer.next().month(6).day(17).year(1993);
            writer.next().month(7).day(20).year(2026);

            for (long i = 0; i < writer.position(); i++) {
                DateStruct date = dates.at(i);
                System.out.println(date);
            }

        }
    }
}