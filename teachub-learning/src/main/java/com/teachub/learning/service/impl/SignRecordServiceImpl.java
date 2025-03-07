package com.teachub.learning.service.impl;

import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.exceptions.BizIllegalException;
import com.teachub.common.utils.CollUtils;
import com.teachub.common.utils.UserContext;
import com.teachub.learning.constants.RedisConstant;
import com.teachub.learning.domain.vo.SignResultVO;
import com.teachub.learning.service.ISignRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignRecordServiceImpl implements ISignRecordService {
    private final StringRedisTemplate redisTemplate;

    @Override
    public SignResultVO addSignRecord() {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("用户未登录");
        }
        //拼接redis键
        LocalDate now = LocalDate.now();
        String nowDate = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = RedisConstant.SIGN_RECORD_KEY_PREFIX + userId + nowDate;
        //获取offset
        int offset = now.getDayOfMonth() - 1;
        //操作redis
        Boolean result = redisTemplate.opsForValue().setBit(key, offset, true);
        if (Boolean.TRUE.equals(result)) {
            throw new BizIllegalException("不能重复签到");
        }
        //统计连续签到次数
        List<Long> longs = redisTemplate.opsForValue().bitField(key, BitFieldSubCommands.create().
                get(BitFieldSubCommands.BitFieldType.unsigned(now.getDayOfMonth())).valueAt(0));
        if(CollUtils.isEmpty(longs)){
            return new SignResultVO(0,0,0);
        }
        Long signDays = longs.get(0);
        log.info("签到日期:{}",signDays);
        int count = 0;
        while((signDays&1)==1){
            count++;
            signDays = signDays >> 1;
        }
        int rewardPoints = 0;
        switch (count){
            case 7:{
                rewardPoints=10;
                break;
            }
            case 14:{
                rewardPoints=20;
                break;
            }
            case 28:{
                rewardPoints=40;
                break;
            }
        }
        return new SignResultVO(count,1,rewardPoints);
    }
}
