package com.teachub.api.dto.leanring;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "学习课表进度信息")
@Builder
public class LearningLessonDTO {
    @ApiModelProperty("课表id")
    private Long id;
    @ApiModelProperty("最近学习的小节id")
    private Long latestSectionId;
    @ApiModelProperty("学习过的小节的记录")
    private List<LearningRecordDTO> records;
}
