package com.teachub.learning.task;


import com.teachub.learning.service.ILearningLessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LessonTask {
    private final ILearningLessonService learningLessonService;
    @Scheduled(cron = "0 0 */1 * * ?")
    public void updateExpiredLessons() {
        learningLessonService.updateExpiredLessons();
    }
}
