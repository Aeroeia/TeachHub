package com.teachub.learning.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.api.client.user.UserClient;
import com.teachub.api.dto.user.UserDTO;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.exceptions.BizIllegalException;
import com.teachub.common.utils.CollUtils;
import com.teachub.common.utils.UserContext;
import com.teachub.learning.constants.RedisConstant;
import com.teachub.learning.domain.dto.PointsBoardQuery;
import com.teachub.learning.domain.po.PointsBoard;
import com.teachub.learning.domain.vo.PointsBoardItemVO;
import com.teachub.learning.domain.vo.PointsBoardVO;
import com.teachub.learning.mapper.PointsBoardMapper;
import com.teachub.learning.service.IPointsBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 学霸天梯榜 服务实现类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-26
 */
@Service
@RequiredArgsConstructor
public class PointsBoardServiceImpl extends ServiceImpl<PointsBoardMapper, PointsBoard> implements IPointsBoardService {
    private final StringRedisTemplate redisTemplate;

    private final UserClient userClient;

    //查询积分排行榜
    @Override
    public PointsBoardVO queryPointsBoard(PointsBoardQuery pointsBoardQuery) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("请先登陆");
        }
        //看是否是当前赛季
        boolean isCurrent = pointsBoardQuery.getSeason() == null || pointsBoardQuery.getSeason() == 0;
        String format = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String key = RedisConstant.POINTS_BOARD_KEY_PREFIX + format;
        //查询当前排名
        Long season = pointsBoardQuery.getSeason();
        PointsBoard myRank = isCurrent ? queryMyCurrentBoard(key) : queryMyHistoryBoard(season);
        //分页查询榜单
        Integer pageNo = pointsBoardQuery.getPageNo();
        Integer pageSize = pointsBoardQuery.getPageSize();
        List<PointsBoard> boardList = isCurrent ? queryBoardList(key, pageSize, pageNo) : queryHistoryBoardList(pointsBoardQuery);
        //封装数据
        PointsBoardVO pointsBoardVO = new PointsBoardVO();
        pointsBoardVO.setRank(myRank.getRank());
        pointsBoardVO.setPoints(myRank.getPoints());
        //远程调用userService查询用户名
        List<Long> userIds = boardList.stream().map(PointsBoard::getUserId).collect(Collectors.toList());
        if(CollUtils.isEmpty(userIds)){
            throw new BizIllegalException("用户不存在");
        }
        List<UserDTO> userDTOS = userClient.queryUserByIds(userIds);
        Map<Long, String> userMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, UserDTO::getName));
        //封装vo
        List<PointsBoardItemVO> list = new ArrayList<>();
        for(PointsBoard board : boardList){
            PointsBoardItemVO pointsBoardItemVO = new PointsBoardItemVO();
            pointsBoardItemVO.setName(userMap.get(board.getUserId()));
            pointsBoardItemVO.setRank(board.getRank());
            pointsBoardItemVO.setPoints(board.getPoints());
            list.add(pointsBoardItemVO);
        }
        pointsBoardVO.setBoardList(list);
        return pointsBoardVO;
    }

    //分页查询当前月榜单
    private List<PointsBoard> queryBoardList(String key, Integer pageSize, Integer pageNo) {
        //构建分页条件
        int start = (pageNo - 1) * pageSize;
        int end = start + pageSize - 1;
        Set<ZSetOperations.TypedTuple<String>> list = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        if (CollUtils.isEmpty(list)) {
            return List.of();
        }
        List<PointsBoard> result = new ArrayList<>(list.size());
        int rank = start;
        //遍历结果
        for (var tuple : list) {
            PointsBoard board = new PointsBoard();
            board.setRank(++rank);
            String userId = tuple.getValue();
            Double score = tuple.getScore();
            if (StrUtil.isBlank(userId) || score == null) {
                continue;
            }
            board.setUserId(Long.parseLong(userId));
            board.setPoints(score.intValue());
            result.add(board);
        }
        return result;
    }

    //TODO 分页查询历史榜单
    private List<PointsBoard> queryHistoryBoardList(PointsBoardQuery pointsBoardQuery) {
        return null;
    }


    //查询我的当前月的排行
    private PointsBoard queryMyCurrentBoard(String key) {
        Long userId = UserContext.getUser();
        //redis中获取我的信息
        Double score = redisTemplate.opsForZSet().score(key, userId.toString());
        Long rank = redisTemplate.opsForZSet().reverseRank(key, userId.toString());
        //赋值
        PointsBoard pointsBoard = new PointsBoard();
        pointsBoard.setPoints(score == null ? 0 : score.intValue())
                .setRank(rank == null ? 0 : rank.intValue() + 1);
        return pointsBoard;
    }

    //TODO 查询我的历史排行
    private PointsBoard queryMyHistoryBoard(Long season) {
        return null;
    }


}
