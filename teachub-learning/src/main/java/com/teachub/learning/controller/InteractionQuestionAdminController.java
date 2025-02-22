package com.teachub.learning.controller;

import com.teachub.common.domain.dto.PageDTO;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.learning.domain.dto.QuestionAdminPageQuery;
import com.teachub.learning.domain.vo.QuestionAdminVO;
import com.teachub.learning.service.IInteractionQuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/questions")
@Slf4j
@RequiredArgsConstructor
@Api(tags = "互动问题管理接口")
public class InteractionQuestionAdminController {
    private final IInteractionQuestionService interactionQuestionService;
    @ApiOperation("分页查询问题")
    @GetMapping("/page")
    public PageDTO<QuestionAdminVO> queryAdminPages(QuestionAdminPageQuery query){
        log.info("分页查询参数:{}",query);
        return interactionQuestionService.queryAdminQuestions(query);
    }
    @ApiOperation("隐藏/显示问题")
    @PutMapping("/{id}/hidden/{hidden}")
    public void updateHidden(@PathVariable Long id,@PathVariable Boolean hidden){
        log.info("修改问题id:{},隐藏状态:{}",id,hidden);
        if(id==null){
            throw new BadRequestException("参数错误");
        }
        interactionQuestionService.updateHidden(id,hidden);
    }

}
