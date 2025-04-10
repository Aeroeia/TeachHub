package com.teachub.api.client.remark;

import java.util.List;
import java.util.Set;

public interface RemarkClient {

    Set<Long> getBatchLiked(List<Long> bizIds);
}
