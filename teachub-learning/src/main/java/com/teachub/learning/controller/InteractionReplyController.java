package com.teachub.learning.controller;


import com.teachub.common.domain.dto.PageDTO;
import com.teachub.learning.domain.dto.ReplyDTO;
import com.teachub.learning.domain.dto.ReplyPageQuery;
import com.teachub.learning.domain.vo.ReplyVO;
import com.teachub.learning.service.IInteractionReplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 互动问题的回答或评论 前端控制器
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-19
 */
@Slf4j
@RestController
@RequestMapping("/replies")
@RequiredArgsConstructor
@Api(tags = "互动问题回答、评论接口")
public class InteractionReplyController {
    private final IInteractionReplyService replyService;
    @ApiOperation("提交问题回答")
    @PostMapping
    public void postReply(@RequestBody ReplyDTO replyDTO){
        log.info("问题提交表单:{}",replyDTO);
        replyService.postReply(replyDTO);
    }
    @ApiOperation("分页查询问题回答")
    @GetMapping("/page")
    public PageDTO<ReplyVO> pageQuery(ReplyPageQuery replyPageQuery){
        log.info("分页查询参数:{}",replyPageQuery);
        return replyService.pageQuery(replyPageQuery,false);
    }

}
