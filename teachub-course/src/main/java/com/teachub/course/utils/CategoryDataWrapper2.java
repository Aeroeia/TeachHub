package com.teachub.course.utils;

import com.teachub.common.utils.TreeDataUtils;
import com.teachub.course.domain.po.Category;
import com.teachub.course.domain.vo.CategoryVO;

import java.util.List;

/**
 * @ClassName CategoryDataWrapper2
 *  wusongsong
 * @Date /9/21 19:44
 * @Version
 **/
public class CategoryDataWrapper2 implements TreeDataUtils.DataProcessor<CategoryVO, Category> {
    @Override
    public Object getParentKey(Category category) {
        return category.getParentId();
    }

    @Override
    public Object getKey(Category category) {
        return category.getId();
    }

    @Override
    public Object getRootKey() {
        return 0L;
    }

    @Override
    public List<CategoryVO> getChild(CategoryVO categoryVO) {
        return categoryVO.getChildren();
    }

    @Override
    public void setChild(CategoryVO parent, List<CategoryVO> child) {
        parent.setChildren(child);
    }
}
