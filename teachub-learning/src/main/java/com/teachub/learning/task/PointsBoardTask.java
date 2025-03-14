package com.teachub.learning.task;

import com.teachub.common.utils.CollUtils;
import com.teachub.learning.constants.LearningConstants;
import com.teachub.learning.constants.RedisConstant;
import com.teachub.learning.domain.po.PointsBoard;
import com.teachub.learning.domain.po.PointsBoardSeason;
import com.teachub.learning.service.IPointsBoardSeasonService;
import com.teachub.learning.service.IPointsBoardService;
import com.teachub.learning.utils.TableInfoContext;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointsBoardTask {
    private final IPointsBoardSeasonService pointsBoardSeasonService;
    private final IPointsBoardService pointsBoardService;
    @XxlJob("createTableJob")
    public void createPointBoardTable(){
        log.info("将上赛季数据写入数据库");
        //查询上赛季id
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        PointsBoardSeason season = pointsBoardSeasonService.lambdaQuery()
                .ge(PointsBoardSeason::getEndTime, lastMonth)
                .le(PointsBoardSeason::getBeginTime, lastMonth)
                .one();
        if(season==null){
            return;
        }
        //创建赛季表
        pointsBoardSeasonService.createSeasonTable(season.getId());

    }
    //将上赛季信息保存到赛季表
    @XxlJob("savePointsBoard2DB")
    public void savePointsBoard() {
        log.info("将历史数据存到db");
        //获取上个月赛季时间节点
        PointsBoardSeason season = pointsBoardSeasonService.lambdaQuery()
                .le(PointsBoardSeason::getBeginTime, LocalDate.now().minusMonths(1))
                .ge(PointsBoardSeason::getEndTime, LocalDate.now().minusMonths(1))
                .one();
        if(season==null){
            return;
        }
        //拼接表名
        String key = LearningConstants.POINTS_BOARD_TABLE_PREFIX+season.getId();
        log.info("表名为:{}",key);
        TableInfoContext.setInfo(key);
        //从redis中读取数据
        String format = RedisConstant.POINTS_BOARD_KEY_PREFIX+LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
        log.info("key为:{}",format);
        //分页查询redis减缓压力
        //通过集群xxl-job分片加快持久化速度
        int shardTotal = XxlJobHelper.getShardTotal();
        int pageNo = XxlJobHelper.getShardIndex();
        int pageSize = 1000;
        while(true){
            List<PointsBoard> pointsBoards = pointsBoardService.queryBoardList(format, pageSize, pageNo);
            if(CollUtils.isEmpty(pointsBoards)){
                break;
            }
            //将rank转移到id后db批量新增
            for(PointsBoard pointsBoard : pointsBoards){
                pointsBoard.setId(pointsBoard.getRank().longValue());
                pointsBoard.setRank(null);
            }
            log.info("数据:{}",pointsBoards);
            pointsBoardService.saveBatch(pointsBoards);
            pageNo+=shardTotal;
        }
        //清空ThreadLocal
        TableInfoContext.remove();
    }
}
