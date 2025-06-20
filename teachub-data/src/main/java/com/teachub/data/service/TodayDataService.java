package com.teachub.data.service;


import com.teachub.data.model.dto.TodayDataDTO;
import com.teachub.data.model.vo.TodayDataVO;

/**
 *  wusongsong
 * @  /10/13 9:27
 **/
public interface TodayDataService {

    /**
     * 获取今日数据
     * @return
     */
    TodayDataVO get();

    /**
     * 设置今日数据
     * @param todayDataDTO
     */
    void set(TodayDataDTO todayDataDTO);
}