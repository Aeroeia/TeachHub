package com.teachub.remark.task;

import com.teachub.remark.config.LikeProperty;
import com.teachub.remark.service.ILikedRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LikedTimesTask {
    private static final Integer MAX_BIZ_SIZE = 30;
    private final ILikedRecordService likedRecordService;
    private final LikeProperty bizType;
    @Scheduled(fixedDelay = 20000)
    public void syncLikedTimes(){
        log.info("开始同步点赞数");
        for(String type : bizType.getBizTypes()){
            likedRecordService.readLikedTimesAndSendMsg(type,MAX_BIZ_SIZE);
        }
    }
}
