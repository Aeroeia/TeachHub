package com.teachub.user.service;

import com.teachub.common.domain.dto.PageDTO;
import com.teachub.user.domain.dto.StudentFormDTO;
import com.teachub.user.domain.query.UserPageQuery;
import com.teachub.user.domain.vo.StudentPageVo;

/**
 * <p>
 * 学员详情表 服务类
 * </p>
 *
 *
 * @  07-12
 */
public interface IStudentService {

    void saveStudent(StudentFormDTO studentFormDTO);

    void updateMyPassword(StudentFormDTO studentFormDTO);

    PageDTO<StudentPageVo> queryStudentPage(UserPageQuery pageQuery);
}
