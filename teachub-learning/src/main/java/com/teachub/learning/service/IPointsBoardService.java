package com.teachub.learning.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.teachub.learning.domain.dto.PointsBoardQuery;
import com.teachub.learning.domain.po.PointsBoard;
import com.teachub.learning.domain.vo.PointsBoardVO;

import java.util.List;

/**
 * <p>
 * 学霸天梯榜 服务类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-26
 */
public interface IPointsBoardService extends IService<PointsBoard> {

    PointsBoardVO queryPointsBoard(PointsBoardQuery pointsBoardQuery);


    List<PointsBoard> queryBoardList(String key, Integer pageSize, Integer pageNo);
}
