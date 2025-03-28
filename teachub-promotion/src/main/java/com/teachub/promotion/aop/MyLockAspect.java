package com.teachub.promotion.aop;

import com.teachub.common.utils.UserContext;
import com.teachub.promotion.annotation.MyLock;
import com.teachub.promotion.utils.MyLockFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class MyLockAspect {
    private final MyLockFactory myLockFactory;

    @Around("@annotation(myLock)")
    public Object tryLock(ProceedingJoinPoint pjp, MyLock myLock) throws Throwable {
        log.info("执行aop加锁操作");
        //悲观锁
        String key = "lock:coupon:uid:" + UserContext.getUser();
        log.info("加锁的key为：{}", key);
        //工厂获取锁
        RLock lock = myLockFactory.getLock(myLock.lockType(), key);
        //策略模式
        boolean flag = myLock.lockStrategy().tryLock(lock, myLock);
        if(!flag){
            return null;
        }
        try {
            return pjp.proceed();
        }
        finally {
            lock.unlock();
        }
    }
}
