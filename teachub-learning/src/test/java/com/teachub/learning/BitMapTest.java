package com.teachub.learning;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
//@SpringBootTest
public class BitMapTest {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Test
    void bitSet(){
        Boolean b = redisTemplate.opsForValue().setBit("test", 1, true);
        System.out.println(b);
    }

    @Test
    void bitGet(){
        List<Long> longs = redisTemplate.opsForValue().bitField("test",
                BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(3))
                        .valueAt(0));
        Long l = longs.get(0);
        System.out.println(Long.toBinaryString(l));
    }
    @Test
    void formatTest(){
        LocalDate now = LocalDate.now();
        System.out.println(now);
        System.out.println(now.format(DateTimeFormatter.ofPattern(":yyyyMM")));
        System.out.println(now.getDayOfMonth());
    }
    @Test
    void bitT(){
        int dayOfMonth = LocalDate.now().getDayOfMonth();
        Long record = 5L;
        String replace = String.format("%" + dayOfMonth + "s", Long.toBinaryString(record)).replace(' ', '0');
        System.out.println(replace);
        System.out.println(replace.length());

    }
}
