package com.teachub.api.client.remark.fallback;

import com.teachub.api.client.remark.RemarkClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;
import java.util.Set;

@Slf4j
public class RemarkClientFallBackFactory implements FallbackFactory<RemarkClient> {
    @Override
    public RemarkClient create(Throwable cause) {
        log.info("调用remark服务降级:",cause);
        return new RemarkClient() {
            @Override
            public Set<Long> getBatchLiked(List<Long> bizIds) {
                return Set.of();
            }
        };
    }
}
