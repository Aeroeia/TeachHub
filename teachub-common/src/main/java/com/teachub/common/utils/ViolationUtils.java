package com.teachub.common.utils;

import com.teachub.common.exceptions.BadRequestException;

import javax.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 手动执行Violation处理校验结果
 * @ClassName ViolationUtils
 *  wusongsong
 * @  /7/18 20:52
 * @version 1.0.0
 **/
public class ViolationUtils {

    public static <T> void process(Set<ConstraintViolation<T>> violations) {
        if(CollUtils.isEmpty(violations)){
            return;
        }
        String message = violations.stream().map(v -> v.getMessage()).collect(Collectors.joining("|"));
        throw new BadRequestException(message);
    }
}
