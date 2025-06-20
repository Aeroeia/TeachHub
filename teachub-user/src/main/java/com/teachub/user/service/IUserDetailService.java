package com.teachub.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teachub.user.domain.po.UserDetail;
import com.teachub.user.domain.query.UserPageQuery;
import com.teachub.common.enums.UserType;

import java.util.List;

/**
 * <p>
 * 教师详情表 服务类
 * </p>
 *
 *   
 * @  08-15
 */
public interface IUserDetailService extends IService<UserDetail> {

    UserDetail queryById(Long userId);

    List<UserDetail> queryByIds(List<Long> ids);

    Page<UserDetail> queryUserDetailByPage(UserPageQuery pageQuery, UserType type);
}
