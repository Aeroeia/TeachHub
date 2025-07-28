package com.teachub.learning.task;
import com.teachub.learning.domain.po.PointsBoardSeason;
import com.teachub.learning.service.IPointsBoardSeasonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointsBoardTask {
    private final IPointsBoardSeasonService pointsBoardSeasonService;
    @Scheduled(cron = "0 0 0 1 * ?")
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
}
