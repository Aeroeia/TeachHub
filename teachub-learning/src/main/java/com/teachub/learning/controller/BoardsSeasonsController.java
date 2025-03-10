package com.teachub.learning.controller;

import com.teachub.learning.domain.vo.PointsBoardSeasonVO;
import com.teachub.learning.service.IPointsBoardSeasonService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(tags = "赛季相关接口")
@Slf4j
@RequestMapping("/boards/seasons")
@RequiredArgsConstructor
public class BoardsSeasonsController {
    private final IPointsBoardSeasonService pointsBoardSeasonService;
    @GetMapping("list")
    public List<PointsBoardSeasonVO> querySeasons(){
        log.info("查询赛季信息");
        return pointsBoardSeasonService.querySeasons();
    }
}
