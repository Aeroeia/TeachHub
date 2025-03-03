package com.teachub.remark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.api.dto.remark.LikedTimesDTO;
import com.teachub.common.autoconfigure.mq.RabbitMqHelper;
import com.teachub.common.constants.MqConstants;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.utils.CollUtils;
import com.teachub.common.utils.StringUtils;
import com.teachub.common.utils.UserContext;
import com.teachub.remark.constans.RedisConstants;
import com.teachub.remark.domain.dto.LikeRecordFormDTO;
import com.teachub.remark.domain.po.LikedRecord;
import com.teachub.remark.mapper.LikedRecordMapper;
import com.teachub.remark.service.ILikedRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
@Slf4j
public class LikedRecordServiceImpl extends ServiceImpl<LikedRecordMapper, LikedRecord> implements ILikedRecordService {
    private final RabbitMqHelper rabbitMqHelper;
    private final StringRedisTemplate redisTemplate;
    //点赞或取消
    @Override
    public void postLiked(LikeRecordFormDTO likeRecordFormDTO) {
        Long userId = UserContext.getUser();
        if(userId==null){
            throw new BadRequestException("获取用户登陆id失败");
        }
        //判断是否新增/取消点赞成功
        boolean flag = likeRecordFormDTO.getLiked()?like(likeRecordFormDTO):unlike(likeRecordFormDTO);
        if(!flag){
            return;
        }
        //统计该业务点赞用户数量
        String key = RedisConstants.LIKE_BIZ_KEY_PREFIX + likeRecordFormDTO.getBizId();
        Long size = redisTemplate.opsForSet().size(key);
        //更新业务点赞数
        String bizKey = RedisConstants.LIKES_TIMES_KEY_PREFIX+likeRecordFormDTO.getBizType();
        redisTemplate.opsForZSet().add(bizKey,likeRecordFormDTO.getBizId().toString(), size);
    }
    private boolean like(LikeRecordFormDTO likeRecordFormDTO){
        String key = RedisConstants.LIKE_BIZ_KEY_PREFIX + likeRecordFormDTO.getBizId();
        Long userId = UserContext.getUser();
        Long add = redisTemplate.opsForSet().add(key, userId.toString());
        return add!=null&&add>0;
    }
    private boolean unlike(LikeRecordFormDTO likeRecordFormDTO){
        String key = RedisConstants.LIKE_BIZ_KEY_PREFIX + likeRecordFormDTO.getBizId();
        Long userId = UserContext.getUser();
        Long remove = redisTemplate.opsForSet().remove(key, userId.toString());
        return remove!=null&&remove>0;
    }
    //查询点赞状态
    @Override
    public Set<Long> getBatchLiked(List<Long> bizIds) {
        Long userid = UserContext.getUser();
        if(userid==null){
            throw new BadRequestException("获取用户id失败");
        }
        List<Object> objects = redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                StringRedisConnection redisConnection = (StringRedisConnection) connection;
                for (Long bizId : bizIds) {
                    String key = RedisConstants.LIKE_BIZ_KEY_PREFIX + bizId;
                    redisConnection.sIsMember(key, userid.toString()); //指令入队列
                }
                return null;
            }
        });
        return IntStream.range(0,objects.size())
                .filter(i -> (Boolean) objects.get(i)) //过滤为只为true的元素
                .mapToObj(i -> bizIds.get(i)) // 根据索引获取元素
                .collect(Collectors.toSet());
    }
    //发送mq同步点赞数
    @Override
    public void readLikedTimesAndSendMsg(String type, Integer maxBizSize) {
        String key = RedisConstants.LIKES_TIMES_KEY_PREFIX + type;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().popMin(key, maxBizSize);
        if(typedTuples==null){
            return;
        }
        List<LikedTimesDTO> list = new ArrayList<>();
        for(ZSetOperations.TypedTuple<String> tuple:typedTuples){
            String bizId = tuple.getValue();
            Double likedTimes = tuple.getScore();
            LikedTimesDTO likedTimesDTO = LikedTimesDTO.of(Long.parseLong(bizId), likedTimes.intValue());
            log.info("dto:{}",likedTimesDTO);
            list.add(likedTimesDTO);
        }
        if(CollUtils.isEmpty(list)){
            log.info("list为空");
            return;
        }
        log.info("发送mq消息：{}",list);
        String mqKey = StringUtils.format(MqConstants.Key.LIKED_TIMES_KEY_TEMPLATE,type);
        rabbitMqHelper.send(MqConstants.Exchange.LIKE_RECORD_EXCHANGE,mqKey,list);
    }
}
