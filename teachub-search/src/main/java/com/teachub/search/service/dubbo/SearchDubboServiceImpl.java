package com.teachub.search.service.dubbo;

import com.teachub.api.client.search.SearchClient;
import com.teachub.search.service.ISearchService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DubboService
public class SearchDubboServiceImpl implements SearchClient {

    @Autowired
    private ISearchService searchService;

    @Override
    public List<Long> queryCoursesIdByName(String keyword) {
        return searchService.queryCoursesIdByName(keyword);
    }
}
