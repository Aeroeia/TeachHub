package com.teachub.promotion;


import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RedissonTest {
    @Autowired
    private RedissonClient redissonClient;
    @Test
    public void test() throws InterruptedException {
        RLock lock = redissonClient.getLock("redistest");
        lock.tryLock();
        System.out.println("获取锁成功");
        lock.unlock();

    }
}
