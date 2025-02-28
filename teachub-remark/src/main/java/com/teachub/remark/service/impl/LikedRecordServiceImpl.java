package com.teachub.remark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.api.dto.remark.LikedTimesDTO;
import com.teachub.common.autoconfigure.mq.RabbitMqHelper;
import com.teachub.common.constants.MqConstants;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.utils.BeanUtils;
import com.teachub.common.utils.StringUtils;
import com.teachub.common.utils.UserContext;
import com.teachub.remark.domain.dto.LikeRecordFormDTO;
import com.teachub.remark.domain.po.LikedRecord;
import com.teachub.remark.mapper.LikedRecordMapper;
import com.teachub.remark.service.ILikedRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 点赞记录表 服务实现类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-24
 */
@Service
@RequiredArgsConstructor
public class LikedRecordServiceImpl extends ServiceImpl<LikedRecordMapper, LikedRecord> implements ILikedRecordService {
    private final RabbitMqHelper rabbitMqHelper;
    //点赞或取消
    @Override
    public void postLiked(LikeRecordFormDTO likeRecordFormDTO) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("请先登录");
        }
        //判断是否点过赞
        boolean liked = likeRecordFormDTO.getLiked();
        boolean flag;
        if (liked) {
            LikedRecord one = this.lambdaQuery().eq(LikedRecord::getUserId, userId)
                    .eq(LikedRecord::getBizId, likeRecordFormDTO.getBizId())
                    .one();
            if (one != null) {
                return;
            }
            LikedRecord likedRecord = BeanUtils.copyBean(likeRecordFormDTO, LikedRecord.class);
            likedRecord.setUserId(userId);
            flag = this.save(likedRecord);
        }
        else{
            flag = this.remove(this.lambdaQuery().eq(LikedRecord::getUserId, userId)
                    .eq(LikedRecord::getBizId, likeRecordFormDTO.getBizId()));
        }
        //如果业务处理失败，直接返回
        if(!flag){
            return;
        }
        String routingKey = StringUtils.format(MqConstants.Key.LIKED_TIMES_KEY_TEMPLATE,likeRecordFormDTO.getBizType());
        rabbitMqHelper.send(
                MqConstants.Exchange.LIKE_RECORD_EXCHANGE,
                routingKey,
                LikedTimesDTO.of(likeRecordFormDTO.getBizId(), liked)
        );
    }
    //查询点赞状态
    @Override
    public Set<Long> getBatchLiked(List<Long> bizIds) {
        Long userid = UserContext.getUser();
        if(userid==null){
            throw new BadRequestException("获取用户id失败");
        }
        List<LikedRecord> list = this.lambdaQuery().eq(LikedRecord::getUserId, userid)
                .in(LikedRecord::getBizId, bizIds)
                .list();
        return list.stream().map(LikedRecord::getId).collect(Collectors.toSet());
    }
}
