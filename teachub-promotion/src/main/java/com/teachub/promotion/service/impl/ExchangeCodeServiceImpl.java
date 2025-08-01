package com.teachub.promotion.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.common.utils.CollUtils;
import com.teachub.promotion.constants.PromotionConstants;
import com.teachub.promotion.domain.dto.CodeQuery;
import com.teachub.promotion.domain.po.Coupon;
import com.teachub.promotion.domain.po.ExchangeCode;
import com.teachub.promotion.domain.vo.CodeVO;
import com.teachub.promotion.mapper.ExchangeCodeMapper;
import com.teachub.promotion.service.IExchangeCodeService;
import com.teachub.promotion.utils.CodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.teachub.promotion.constants.PromotionConstants.COUPON_RANGE_KEY;

/**
 * <p>
 * 兑换码 服务实现类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-30
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeCodeServiceImpl extends ServiceImpl<ExchangeCodeMapper, ExchangeCode> implements IExchangeCodeService {
    private final StringRedisTemplate redisTemplate;

    //异步生成兑换码
    @Async("generateExchangeCodeExecutor")
    @Override
    public void asyncCreateCode(Coupon one) {
        log.info("异步生成兑换码，线程名为:{}",Thread.currentThread().getName());
        //优惠券最大值
        Integer totalNum = one.getTotalNum();
        //新增值到redis
        Long increment = redisTemplate.opsForValue().increment(PromotionConstants.COUPON_CODE_SERIAL_KEY, totalNum);
        if (increment == null) {
            return;
        }
        //循环生成兑换码
        int maxNum = increment.intValue();
        int begin = maxNum - totalNum + 1;
        List<ExchangeCode> list = new ArrayList<>();
        for (int i = begin; i <= maxNum; i++) {
            String code = CodeUtil.generateCode(i, one.getId());
            ExchangeCode exchangeCode = new ExchangeCode();
            exchangeCode.setCode(code)
                    .setExchangeTargetId(one.getId())
                    .setExpiredTime(one.getIssueEndTime())
                    .setId(i);
            list.add(exchangeCode);
        }
        this.saveBatch(list);

        //存储每个优惠券最大的序列号
        redisTemplate.opsForZSet().add(COUPON_RANGE_KEY, one.getId().toString(), maxNum);
    }
    //查询兑换码
    @Override
    public PageDTO<CodeVO> queryCodesPage(CodeQuery query) {
        Page<ExchangeCode> page = this.lambdaQuery()
                .eq(ExchangeCode::getExchangeTargetId, query.getCouponId())
                .eq(ExchangeCode::getStatus, query.getStatus())
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<ExchangeCode> records = page.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(page);
        }
        List<CodeVO> collect = records.stream().map(po -> {
            CodeVO codeVO = new CodeVO();
            codeVO.setId(po.getId())
                    .setCode(po.getCode());
            return codeVO;
        }).collect(Collectors.toList());
        log.info("返回结果:{}",collect);
        return PageDTO.of(page, collect);
    }
}
