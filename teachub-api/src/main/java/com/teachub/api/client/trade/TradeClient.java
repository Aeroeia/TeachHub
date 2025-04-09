package com.teachub.api.client.trade;

import com.teachub.api.dto.course.CoursePurchaseInfoDTO;
import java.util.List;
import java.util.Map;

public interface TradeClient {
    /**
     * 统计指定课程的报名人数
     * @param courseIdList 课程id集合
     * @return 统计结果
     */
    Map<Long, Integer> countEnrollNumOfCourse(List<Long> courseIdList);

    /**
     * 统计指定学生的报名课程数量
     * @param studentIds 学生id集合
     * @return 统计结果
     */
    Map<Long, Integer> countEnrollCourseOfStudent(List<Long> studentIds);

    /**
     * 检查当前用户是否报名指定课程
     * @param id 课程id
     * @return 是否报名
     */
    Boolean checkMyLesson(Long id);

    /**
     * 统计课程购买、退款状态
     * @param courseId 课程id
     * @return 统计结果
     */
    CoursePurchaseInfoDTO getPurchaseInfoOfCourse(Long courseId);
}
