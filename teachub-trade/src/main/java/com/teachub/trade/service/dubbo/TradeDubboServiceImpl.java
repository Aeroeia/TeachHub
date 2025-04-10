package com.teachub.trade.service.dubbo;

import com.teachub.api.client.trade.TradeClient;
import com.teachub.api.dto.course.CoursePurchaseInfoDTO;
import com.teachub.trade.service.IOrderDetailService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@DubboService
public class TradeDubboServiceImpl implements TradeClient {

    @Autowired
    private IOrderDetailService detailService;

    @Override
    public Map<Long, Integer> countEnrollNumOfCourse(List<Long> courseIdList) {
        return detailService.countEnrollNumOfCourse(courseIdList);
    }

    @Override
    public Map<Long, Integer> countEnrollCourseOfStudent(List<Long> studentIds) {
        return detailService.countEnrollCourseOfStudent(studentIds);
    }

    @Override
    public Boolean checkMyLesson(Long id) {
        return detailService.checkCourseOrderInfo(id);
    }

    @Override
    public CoursePurchaseInfoDTO getPurchaseInfoOfCourse(Long courseId) {
        return detailService.getPurchaseInfoOfCourse(courseId);
    }
}
