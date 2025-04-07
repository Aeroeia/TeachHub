package com.teachub.remark.service.dubbo;

import com.teachub.api.client.remark.RemarkClient;
import com.teachub.remark.service.ILikedRecordService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

@DubboService
public class RemarkDubboServiceImpl implements RemarkClient {

    @Autowired
    private ILikedRecordService likedRecordService;

    @Override
    public Set<Long> getBatchLiked(List<Long> bizIds) {
        return likedRecordService.getBatchLiked(bizIds);
    }
}
