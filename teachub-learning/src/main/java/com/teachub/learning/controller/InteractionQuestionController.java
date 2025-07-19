package com.teachub.learning.controller;


import com.teachub.learning.domain.dto.QuestionFormDTO;
import com.teachub.learning.service.IInteractionQuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 互动提问的问题表 前端控制器
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-19
 */
@Slf4j
@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
@Api(tags = "互动问题相关接口")
public class InteractionQuestionController {
    private final IInteractionQuestionService interactionQuestionService;
    @ApiOperation("新增问题")
    @PostMapping()
    public void addQuestion(@RequestBody @Validated QuestionFormDTO questionFormDTO){
        log.info("问题;{}",questionFormDTO);
        interactionQuestionService.addQuestion(questionFormDTO);
    }
    @ApiOperation("修改问题")
    @PutMapping("/{id}")
    public void updateQuestion(@RequestBody QuestionFormDTO questionFormDTO,@PathVariable Long id){
        log.info("问题:{}",questionFormDTO);
        interactionQuestionService.updateQuestion(questionFormDTO,id);
    }
}
