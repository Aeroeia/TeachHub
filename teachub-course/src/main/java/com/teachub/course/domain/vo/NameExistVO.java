package com.teachub.course.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName NameExistVO
 *  wusongsong
 * @Date /9/19 16:20
 * @Version
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NameExistVO {
    public static final NameExistVO EXISTED = new NameExistVO(true);
    public static final NameExistVO NOT_EXIST = new NameExistVO(false);

    private Boolean existed;
}
