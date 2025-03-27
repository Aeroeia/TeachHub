package com.teachub.promotion.aop;

import com.teachub.common.exceptions.BizIllegalException;
import com.teachub.common.utils.UserContext;
import com.teachub.promotion.annotation.MyLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class MyLockAspect {
    private final RedissonClient redissonClient;
    @Around("@annotation(myLock)")
    public Object tryLock(ProceedingJoinPoint pjp, MyLock myLock) throws Throwable {
        log.info("执行aop加锁操作");
        //悲观锁
        String key = "lock:coupon:uid:" + UserContext.getUser();
        log.info("加锁的key为：{}", key);
        RLock lock = redissonClient.getLock(key);
        try {
            boolean flag = lock.tryLock(myLock.waitTime(), myLock.leaseTime(), myLock.unit());
            if(!flag){
                throw new BizIllegalException("操作太频繁");
            }
            return pjp.proceed();
        }
        finally {
            lock.unlock();
        }
    }
}
