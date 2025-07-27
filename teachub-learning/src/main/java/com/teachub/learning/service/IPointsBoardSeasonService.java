package com.teachub.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teachub.learning.domain.po.PointsBoardSeason;
import com.teachub.learning.domain.vo.PointsBoardSeasonVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-26
 */
public interface IPointsBoardSeasonService extends IService<PointsBoardSeason> {

    List<PointsBoardSeasonVO> querySeasons();
}
