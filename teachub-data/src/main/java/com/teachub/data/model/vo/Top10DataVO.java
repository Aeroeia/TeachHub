package com.teachub.data.model.vo;

import com.teachub.data.model.po.CourseInfo;
import lombok.Data;

import java.util.List;

/**
 * @ClassName Top10DataVO
 *  wusongsong
 * @Date /10/10 19:33
 * @Version
 **/
@Data
public class Top10DataVO {
    // 热门课程
    private List<CourseInfo> hot;
    // 热销课程
    private List<CourseInfo> hotSales;
}
