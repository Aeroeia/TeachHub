package com.teachub.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.common.utils.BeanUtils;
import com.teachub.learning.constants.LearningConstants;
import com.teachub.learning.domain.po.PointsBoardSeason;
import com.teachub.learning.domain.vo.PointsBoardSeasonVO;
import com.teachub.learning.mapper.PointsBoardSeasonMapper;
import com.teachub.learning.service.IPointsBoardSeasonService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-26
 */
@Service
public class PointsBoardSeasonServiceImpl extends ServiceImpl<PointsBoardSeasonMapper, PointsBoardSeason> implements IPointsBoardSeasonService {

    @Override
    public List<PointsBoardSeasonVO> querySeasons() {
        List<PointsBoardSeason> list = this.list();
        return BeanUtils.copyList(list, PointsBoardSeasonVO.class);
    }

    @Override
    public void createSeasonTable(Integer id) {
        this.getBaseMapper().createTable(LearningConstants.POINTS_BOARD_TABLE_PREFIX+id);
    }
}
