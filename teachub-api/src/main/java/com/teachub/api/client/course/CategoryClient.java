package com.teachub.api.client.course;

import com.teachub.api.dto.course.CategoryBasicDTO;
import java.util.List;

public interface CategoryClient {

    /**
     * 获取所有课程及课程分类
     * @return  所有课程及课程分类
     */
    List<CategoryBasicDTO> getAllOfOneLevel();
}
