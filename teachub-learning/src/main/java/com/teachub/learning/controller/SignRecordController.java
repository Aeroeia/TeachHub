package com.teachub.learning.controller;

import com.teachub.learning.domain.vo.SignResultVO;
import com.teachub.learning.service.ISignRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("sign-records")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "签到相关接口")
public class SignRecordController {
    private final ISignRecordService signRecordService;
    @PostMapping
    @ApiOperation("用户签到")
    public SignResultVO addSignRecord(){
        return signRecordService.addSignRecord();
    }
    @ApiOperation("查询签到记录")
    @GetMapping
    public List<Integer> querySignRecord(){
        return signRecordService.querySignRecord();
    }
}
