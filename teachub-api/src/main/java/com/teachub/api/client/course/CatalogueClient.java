package com.teachub.api.client.course;

import com.teachub.api.dto.course.CataSimpleInfoDTO;
import java.util.List;

public interface CatalogueClient {

    /**
     * 根据目录id列表查询目录信息
     *
     * @param ids 目录id列表
     * @return id列表中对应的目录基础信息
     */
    List<CataSimpleInfoDTO> batchQueryCatalogue(Iterable<Long> ids);
}
