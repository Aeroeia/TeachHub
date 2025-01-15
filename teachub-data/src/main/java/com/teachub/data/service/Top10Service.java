package com.teachub.data.service;


import com.teachub.data.model.dto.Top10DataSetDTO;
import com.teachub.data.model.vo.Top10DataVO;

/**
 *  wusongsong
 * @  /10/10 19:39
 **/
public interface Top10Service {

    /**
     * 获取top数据
     *
     * @return
     */
    Top10DataVO getTop10Data();

    /**
     * top 10数据设置
     * @param top10DataSetDTO
     */
    void setTop10Data(Top10DataSetDTO top10DataSetDTO);
}