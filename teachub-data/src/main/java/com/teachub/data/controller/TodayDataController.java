package com.teachub.data.controller;


import com.teachub.data.model.dto.TodayDataDTO;
import com.teachub.data.model.vo.TodayDataVO;
import com.teachub.data.service.TodayDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 今日数据
 * @ClassName TodayDataController
 *  wusongsong
 * @Date /10/13 9:21
 * @Version
 **/
@RestController
@RequestMapping("/data/today")
@Api(tags = "今日数据相关接口")
public class TodayDataController {

    @Autowired
    private TodayDataService todayDataService;

    @GetMapping("")
    @ApiOperation("获取今日数据")
    public TodayDataVO get(){
        return todayDataService.get();
    }

    @PutMapping("set")
    @ApiOperation("设置线上数据")
    public void set(@RequestBody TodayDataDTO todayDataDTO) {
        todayDataService.set(todayDataDTO);
    }
}
