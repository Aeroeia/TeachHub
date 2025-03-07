package com.teachub.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.learning.domain.po.PointsRecord;
import com.teachub.learning.mapper.PointsRecordMapper;
import com.teachub.learning.service.IPointsRecordService;
import org.springframework.stereotype.Service;

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

}
