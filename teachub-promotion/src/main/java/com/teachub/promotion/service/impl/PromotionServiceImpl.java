package com.teachub.promotion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.promotion.domain.po.Promotion;
import com.teachub.promotion.mapper.PromotionMapper;
import com.teachub.promotion.service.IPromotionService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 促销活动，形式多种多样，例如：优惠券 服务实现类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-30
 */
@Service
public class PromotionServiceImpl extends ServiceImpl<PromotionMapper, Promotion> implements IPromotionService {

}
