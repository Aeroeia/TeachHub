package com.teachub.learning.controller;


import com.teachub.common.domain.dto.PageDTO;
import com.teachub.learning.domain.dto.QuestionFormDTO;
import com.teachub.learning.domain.dto.QuestionPageQuery;
import com.teachub.learning.domain.vo.QuestionVO;
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
    @ApiOperation("分页查询问题")
    @GetMapping("/page")
    public PageDTO<QuestionVO> queryQuestions(@Validated QuestionPageQuery pageQuery){
        log.info("分页查询参数:{}",pageQuery);
        return interactionQuestionService.queryQuestions(pageQuery);
    }
    @ApiOperation("根据id查看问题详情")
    @GetMapping("/{id}")
    public QuestionVO queryQuestionById(@PathVariable Long id){
        log.info("问题id:{}",id);
        return interactionQuestionService.queryById(id);
    }
    @ApiOperation("删除问题")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        log.info("问题id:{}",id);
        interactionQuestionService.delete(id);
    }
}
