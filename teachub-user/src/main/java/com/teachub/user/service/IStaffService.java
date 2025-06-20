package com.teachub.user.service;

import com.teachub.common.domain.dto.PageDTO;
import com.teachub.user.domain.query.UserPageQuery;
import com.teachub.user.domain.vo.StaffVO;

/**
 * <p>
 * 员工详情表 服务类
 * </p>
 *
 *
 * @  07-12
 */
public interface IStaffService {
    PageDTO<StaffVO> queryStaffPage(UserPageQuery pageQuery);
}
