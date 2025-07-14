package com.teachub.learning.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data

public class LearningPlanDTO {
    @ApiModelProperty("课程id")
    @NotNull
    @Min(1)
    private Long courseId;

    @ApiModelProperty("学习频率")
    @NotNull
    @Range(min = 1, max = 50)
    private Integer freq;
}
