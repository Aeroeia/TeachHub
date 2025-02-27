package com.teachub.learning.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 互动问题的回答或评论
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("interaction_reply")
@ApiModel(value="InteractionReply对象", description="互动问题的回答或评论")
public class InteractionReply implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "互动问题的回答id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "互动问题问题id")
    @TableField("question_id")
    private Long questionId;

    @ApiModelProperty(value = "回复的上级回答id")
    @TableField("answer_id")
    private Long answerId;

    @ApiModelProperty(value = "回答者id")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "回答内容")
    @TableField("content")
    private String content;

    @ApiModelProperty(value = "回复的目标用户id")
    @TableField("target_user_id")
    private Long targetUserId;

    @ApiModelProperty(value = "回复的目标回复id")
    @TableField("target_reply_id")
    private Long targetReplyId;

    @ApiModelProperty(value = "评论数量")
    @TableField("reply_times")
    private Integer replyTimes;

    @ApiModelProperty(value = "点赞数量")
    @TableField("liked_times")
    private Integer likedTimes;

    @ApiModelProperty(value = "是否被隐藏，默认false")
    @TableField("hidden")
    private Boolean hidden;

    @ApiModelProperty(value = "是否匿名，默认false")
    @TableField("anonymity")
    private Boolean anonymity;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;


}
