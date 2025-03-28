package com.teachub.promotion.utils;

import com.teachub.promotion.enums.MyLockType;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;


/*
    工厂模式
 */
@Component
public class MyLockFactory {

    private final Map<MyLockType, Function<String, RLock>> lockHandlers;

    public MyLockFactory(RedissonClient redissonClient) {
        this.lockHandlers = new EnumMap<>(MyLockType.class);
        this.lockHandlers.put(MyLockType.RE_ENTRANT_LOCK, redissonClient::getLock);
        this.lockHandlers.put(MyLockType.FAIR_LOCK, redissonClient::getFairLock);
        this.lockHandlers.put(MyLockType.READ_LOCK, name -> redissonClient.getReadWriteLock(name).readLock());
        this.lockHandlers.put(MyLockType.WRITE_LOCK, name -> redissonClient.getReadWriteLock(name).writeLock());
    }

    public RLock getLock(MyLockType lockType, String name){
        return lockHandlers.get(lockType).apply(name);
    }
}