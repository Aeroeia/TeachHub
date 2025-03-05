package com.teachub.api.client.remark;

import com.teachub.api.client.remark.fallback.RemarkClientFallBackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@FeignClient(contextId = "remark", value = "remark-service", path = "likes",fallbackFactory = RemarkClientFallBackFactory.class)
public interface RemarkClient {

    @GetMapping("/list")
    Set<Long> getBatchLiked(@RequestParam("bizIds") List<Long> bizIds);
}
