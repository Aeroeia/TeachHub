package com.teachub.remark.controller;

import com.teachub.remark.domain.dto.LikeRecordFormDTO;
import com.teachub.remark.service.ILikedRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 点赞记录表 前端控制器
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-24
 */
@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "点赞相关接口")
public class LikedRecordController {
    private final ILikedRecordService likedRecordService;
    @ApiOperation("点赞或取消接口")
    @PostMapping
    public void postLike(@RequestBody @Validated LikeRecordFormDTO likeRecordFormDTO){
        log.info("点赞表单:{}",likeRecordFormDTO);
        likedRecordService.postLiked(likeRecordFormDTO);
    }
    @ApiOperation("批量查询点赞状态")
    @GetMapping("/list")
    public Set<Long> getBatchLiked(@RequestParam("bizIds") List<Long> bizIds){
        log.info("批量查询点赞状态:{}",bizIds);
        return likedRecordService.getBatchLiked(bizIds);
    }
}
