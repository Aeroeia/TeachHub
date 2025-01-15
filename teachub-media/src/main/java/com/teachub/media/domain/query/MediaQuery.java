package com.teachub.media.domain.query;

import com.teachub.common.domain.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "媒资搜索条件")
public class MediaQuery extends PageQuery {
    @ApiModelProperty("媒资名称关键字")
    private String name;
}
