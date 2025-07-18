package com.teachub.course.domain.dto;

import com.teachub.course.constants.CourseErrorInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 *  wusongsong
 * @  /7/20 16:50
 * @version 1.0.0
 **/
@ApiModel(description = "课程id")
@Data
public class CourseIdDTO {
    @ApiModelProperty("课程id")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_OPERATE_ID_NULL)
    private Long id;
}
