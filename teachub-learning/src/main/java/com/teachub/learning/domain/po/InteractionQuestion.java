package com.teachub.learning.domain.po;

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
 * 互动提问的问题表
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("interaction_question")
@ApiModel(value="InteractionQuestion对象", description="互动提问的问题表")
public class InteractionQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键，互动问题的id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "互动问题的标题")
    @TableField("title")
    private String title;

    @ApiModelProperty(value = "问题描述信息")
    @TableField("description")
    private String description;

    @ApiModelProperty(value = "所属课程id")
    @TableField("course_id")
    private Long courseId;

    @ApiModelProperty(value = "所属课程章id")
    @TableField("chapter_id")
    private Long chapterId;

    @ApiModelProperty(value = "所属课程节id")
    @TableField("section_id")
    private Long sectionId;

    @ApiModelProperty(value = "提问学员id")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "最新的一个回答的id")
    @TableField("latest_answer_id")
    private Long latestAnswerId;

    @ApiModelProperty(value = "问题下的回答数量")
    @TableField("answer_times")
    private Integer answerTimes;

    @ApiModelProperty(value = "是否匿名，默认false")
    @TableField("anonymity")
    private Boolean anonymity;

    @ApiModelProperty(value = "是否被隐藏，默认false")
    @TableField("hidden")
    private Boolean hidden;

    @ApiModelProperty(value = "管理端问题状态：0-未查看，1-已查看")
    @TableField("status")
    private Integer status;

    @ApiModelProperty(value = "提问时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;


}
