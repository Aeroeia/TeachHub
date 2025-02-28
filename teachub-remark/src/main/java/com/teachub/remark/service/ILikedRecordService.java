package com.teachub.remark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teachub.remark.domain.dto.LikeRecordFormDTO;
import com.teachub.remark.domain.po.LikedRecord;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 点赞记录表 服务类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-24
 */
public interface ILikedRecordService extends IService<LikedRecord> {

    void postLiked(LikeRecordFormDTO likeRecordFormDTO);

    Set<Long> getBatchLiked(List<Long> bizIds);

}
