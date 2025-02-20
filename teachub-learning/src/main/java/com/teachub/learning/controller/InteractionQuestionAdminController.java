package com.teachub.learning.controller;


import com.teachub.common.domain.dto.PageDTO;
import com.teachub.learning.domain.dto.QuestionAdminPageQuery;
import com.teachub.learning.domain.vo.QuestionAdminVO;
import com.teachub.learning.service.IInteractionQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/questions")
@Slf4j
@RequiredArgsConstructor
public class InteractionQuestionAdminController {
    private final IInteractionQuestionService interactionQuestionService;
    @GetMapping("/page")
    public PageDTO<QuestionAdminVO> queryAdminPages(QuestionAdminPageQuery query){
        log.info("分页查询参数:{}",query);
        return interactionQuestionService.queryAdminQuestions(query);
    }
}
