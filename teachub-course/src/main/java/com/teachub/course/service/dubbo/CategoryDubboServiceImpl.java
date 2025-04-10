package com.teachub.course.service.dubbo;

import com.teachub.api.client.course.CategoryClient;
import com.teachub.api.dto.course.CategoryBasicDTO;
import com.teachub.common.utils.BeanUtils;
import com.teachub.course.domain.vo.CategoryVO;
import com.teachub.course.service.ICategoryService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DubboService
public class CategoryDubboServiceImpl implements CategoryClient {

    @Autowired
    private ICategoryService categoryService;

    @Override
    public List<CategoryBasicDTO> getAllOfOneLevel() {
        List<CategoryVO> vos = categoryService.allOfOneLevel();
        return BeanUtils.copyList(vos, CategoryBasicDTO.class);
    }
}
