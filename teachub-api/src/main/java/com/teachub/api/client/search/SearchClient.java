package com.teachub.api.client.search;

import java.util.List;

public interface SearchClient {

    List<Long> queryCoursesIdByName(String keyword);
}
