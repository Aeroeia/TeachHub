package com.teachub.learning.controller;

import com.teachub.learning.domain.vo.PointsStatisticsVO;
import com.teachub.learning.service.IPointsRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(tags = "积分相关接口")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/points")
public class PointsRecordController {
    private final IPointsRecordService pointsRecordService;

    @ApiOperation("查看我的今日积分情况")
    @GetMapping("/today")
    public List<PointsStatisticsVO> getPointsStatistic(){
        return pointsRecordService.getPointsStatistic();
    }
}
