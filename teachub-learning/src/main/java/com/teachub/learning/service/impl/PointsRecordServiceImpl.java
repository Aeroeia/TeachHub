package com.teachub.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.utils.CollUtils;
import com.teachub.common.utils.UserContext;
import com.teachub.learning.constants.RedisConstant;
import com.teachub.learning.domain.po.PointsRecord;
import com.teachub.learning.domain.vo.PointsStatisticsVO;
import com.teachub.learning.enums.PointsRecordType;
import com.teachub.learning.mapper.PointsRecordMapper;
import com.teachub.learning.mq.msg.SignInMessage;
import com.teachub.learning.service.IPointsRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 学习积分记录，每个月底清零 服务实现类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-26
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PointsRecordServiceImpl extends ServiceImpl<PointsRecordMapper, PointsRecord> implements IPointsRecordService {
    private final StringRedisTemplate redisTemplate;
    @Override
    public void addPoints(SignInMessage signInMessage, PointsRecordType pointsRecordType) {
        //查询积分是否有上限
        int maxPoints = pointsRecordType.getMaxPoints();
        int total = 0;
        if(maxPoints>0){
            //积分有上限 查询是否已达上限
            List<PointsRecord> list = this.lambdaQuery().eq(PointsRecord::getUserId, signInMessage.getUserId())
                    .eq(PointsRecord::getType, pointsRecordType)
                    .gt(PointsRecord::getCreateTime, LocalDate.now().atStartOfDay())
                    .list();
            if(CollUtils.isNotEmpty(list)){
                total = list.stream().map(PointsRecord::getPoints)
                        .reduce(0, Integer::sum);
            }
            if(total>=maxPoints){
                return;
            }
        }
        //未达上限，新增记录
        int addPoints = signInMessage.getPoints();
        if(maxPoints>0&&signInMessage.getPoints()>maxPoints-total){
            addPoints = maxPoints-total;
        }
        PointsRecord record = new PointsRecord();
        record.setPoints(addPoints)
                .setType(pointsRecordType)
                .setUserId(signInMessage.getUserId());
        this.save(record);
        //累加保存积分值到redis
        String format = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String key = RedisConstant.POINTS_BOARD_KEY_PREFIX+format;
        //累加积分
        redisTemplate.opsForZSet().incrementScore(key,signInMessage.getUserId().toString(),addPoints);
    }

    @Override
    public List<PointsStatisticsVO> getPointsStatistic() {
        Long userId = UserContext.getUser();
        if(userId==null){
            throw new BadRequestException("获取用户ID失败");
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        //构造查询条件
        QueryWrapper<PointsRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .between("create_time", startOfDay, endOfDay)
                .groupBy("type")
                .select("type","sum(points) as points");
        List<PointsRecord> list = this.list(wrapper);
        log.info("wrapper返回的值:{}",list);
        List<PointsStatisticsVO> result = new ArrayList<>();
        for(PointsRecord pointsRecord : list){
            PointsStatisticsVO vo = new PointsStatisticsVO();
            vo.setMaxPoints(pointsRecord.getType().getMaxPoints());
            vo.setType(pointsRecord.getType().getDesc());
            vo.setPoints(pointsRecord.getPoints());
            result.add(vo);
        }
        return result;
    }

}
