package com.teachub.trade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LongTest {
    @Test
    void t(){
        Long l1 = 130L;
        Long l2 = 129L;
        System.out.println(l1 > l2);
        Integer i1 = 129;
        Integer i2 = 129;
        System.out.println(i1 == i2);

        System.out.println(l1.equals(l2));
        System.out.println(i1.equals(i2));

    }

}