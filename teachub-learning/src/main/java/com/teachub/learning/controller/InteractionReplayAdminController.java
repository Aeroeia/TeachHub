package com.teachub.learning.controller;


import com.teachub.common.domain.dto.PageDTO;
import com.teachub.learning.domain.dto.ReplyPageQuery;
import com.teachub.learning.domain.vo.ReplyVO;
import com.teachub.learning.service.IInteractionReplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/replies")
@Api(tags = "用户端分页查询回答/评论")
public class InteractionReplayAdminController {
    private final IInteractionReplyService replyService;
    @ApiOperation("分页查询回答/评论")
    @GetMapping("/page")
    public PageDTO<ReplyVO> pageQuery(ReplyPageQuery replyPageQuery){
        log.info("分页查询参数:{}",replyPageQuery);
        return replyService.pageQuery(replyPageQuery,true);
    }
    @ApiOperation("隐藏/显示评论")
    @PutMapping("/{id}/hidden/{hidden}")
    public void updateHidden(@PathVariable Long id, @PathVariable Boolean hidden){
        log.info("隐藏/显示参数:id={},hidden={}",id,hidden);
        replyService.updateHidden(id,hidden);
    }
}
