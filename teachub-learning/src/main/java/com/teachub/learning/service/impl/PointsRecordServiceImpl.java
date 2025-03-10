package com.teachub.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.common.utils.CollUtils;
import com.teachub.learning.domain.po.PointsRecord;
import com.teachub.learning.enums.PointsRecordType;
import com.teachub.learning.mapper.PointsRecordMapper;
import com.teachub.learning.mq.msg.SignInMessage;
import com.teachub.learning.service.IPointsRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
public class PointsRecordServiceImpl extends ServiceImpl<PointsRecordMapper, PointsRecord> implements IPointsRecordService {
    @Override
    public void addPoints(SignInMessage signInMessage, PointsRecordType pointsRecordType) {
        //查询积分是否有上限
        int maxPoints = pointsRecordType.getMaxPoints();
        int total = 0;
        if(maxPoints>0){
            //积分有上限 查询是否已达上限
            List<PointsRecord> list = this.lambdaQuery().eq(PointsRecord::getUserId, signInMessage.getUserId())
                    .eq(PointsRecord::getType, pointsRecordType.getValue())
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
                .setType(pointsRecordType.getValue())
                .setUserId(signInMessage.getUserId());
        this.save(record);
    }
}
