package com.teachub.course.service.dubbo;

import com.teachub.api.client.course.CatalogueClient;
import com.teachub.api.dto.course.CataSimpleInfoDTO;
import com.teachub.common.utils.BeanUtils;
import com.teachub.course.domain.vo.CataSimpleInfoVO;
import com.teachub.course.service.ICourseCatalogueService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@DubboService
public class CatalogueDubboServiceImpl implements CatalogueClient {

    @Autowired
    private ICourseCatalogueService courseCatalogueService;

    @Override
    public List<CataSimpleInfoDTO> batchQueryCatalogue(Iterable<Long> ids) {
        List<Long> idList = new ArrayList<>();
        if(ids != null) ids.forEach(idList::add);
        List<CataSimpleInfoVO> vos = courseCatalogueService.getManyCataSimpleInfo(idList);
        return BeanUtils.copyList(vos, CataSimpleInfoDTO.class);
    }
}
