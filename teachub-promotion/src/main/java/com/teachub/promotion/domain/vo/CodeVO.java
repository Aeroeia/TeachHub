package com.teachub.promotion.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CodeVO {
    private Integer id;
    private String code;
}
