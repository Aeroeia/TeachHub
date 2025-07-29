package com.teachub.learning.controller;

import com.teachub.learning.domain.dto.PointsBoardQuery;
import com.teachub.learning.domain.vo.PointsBoardVO;
import com.teachub.learning.service.IPointsBoardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "排行榜相关接口")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/boards")
public class PointsBoardController {
    private final IPointsBoardService pointsBoardService;

    @ApiOperation("查询学霸积分榜")
    @GetMapping
    public PointsBoardVO queryPointsBoard(PointsBoardQuery pointsBoardQuery) {
        log.info("查询学霸积分榜，参数：{}", pointsBoardQuery);
        return pointsBoardService.queryPointsBoard(pointsBoardQuery);
    }
}
