package com.teachub.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teachub.learning.domain.po.PointsRecord;
import com.teachub.learning.domain.vo.PointsStatisticsVO;
import com.teachub.learning.enums.PointsRecordType;
import com.teachub.learning.mq.msg.SignInMessage;

import java.util.List;

/**
 * <p>
 * 学习积分记录，每个月底清零 服务类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-26
 */
public interface IPointsRecordService extends IService<PointsRecord> {

    void addPoints(SignInMessage signInMessage, PointsRecordType pointsRecordType);

    List<PointsStatisticsVO> getPointsStatistic();

}
