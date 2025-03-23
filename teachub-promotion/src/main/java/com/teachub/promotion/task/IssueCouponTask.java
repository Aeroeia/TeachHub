package com.teachub.promotion.task;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teachub.common.utils.BeanUtils;
import com.teachub.common.utils.CollUtils;
import com.teachub.promotion.domain.dto.CouponIssueFormDTO;
import com.teachub.promotion.domain.po.Coupon;
import com.teachub.promotion.enums.CouponStatus;
import com.teachub.promotion.service.ICouponService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class IssueCouponTask {
    private final ICouponService couponService;

    @XxlJob("issueCouponTask")
    public void issueCouponTask() {
        log.info("开始执行发放优惠券任务");
        //获取index分片
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.info("当前分片：{}，总分片：{}", shardIndex, shardTotal);
        while (true) {
            log.info("当前index:{}", shardIndex);
            LocalDateTime now = LocalDateTime.now();
            Page<Coupon> page = couponService.lambdaQuery()
                    .le(Coupon::getIssueBeginTime, now)
                    .page(new Page<>(shardIndex + 1, 10));
            List<Coupon> records = page.getRecords();
            if (CollUtils.isEmpty(records)) {
                return;
            }
            for (Coupon coupon : records) {
                if(coupon.getStatus()== CouponStatus.DRAFT){
                    log.info("发放优惠券");
                    couponService.issueCoupon(coupon.getId(), BeanUtils.copyBean(coupon, CouponIssueFormDTO.class));
                }
            }
            shardIndex+=shardTotal;
        }
    }
    @XxlJob("StopIssueCouponTask")
    public void stopIssueCouponTask()  {
        log.info("停止发放优惠券");
        LocalDateTime now = LocalDateTime.now();
        couponService.lambdaUpdate()
                .le(Coupon::getIssueEndTime, now)
                .set(Coupon::getStatus,CouponStatus.FINISHED)
                .update();
    }
}
