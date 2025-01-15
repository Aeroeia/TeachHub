package com.teachub.course.utils;

import com.teachub.common.utils.TreeDataUtils;
import com.teachub.course.domain.po.Category;
import com.teachub.course.domain.vo.SimpleCategoryVO;

import java.util.List;

/**
 * @ClassName CategoryDataWrapper
 *  wusongsong
 * @Date /9/21 17:45
 * @Version
 **/
public class CategoryDataWrapper implements TreeDataUtils.DataProcessor<SimpleCategoryVO, Category> {

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
    public List<SimpleCategoryVO> getChild(SimpleCategoryVO simpleCategoryVO) {
        return simpleCategoryVO.getChildren();
    }

    @Override
    public void setChild(SimpleCategoryVO parent, List<SimpleCategoryVO> child) {
        parent.setChildren(child);
    }
}
