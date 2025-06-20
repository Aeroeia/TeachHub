package com.teachub.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teachub.api.cache.RoleCache;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.common.enums.UserType;
import com.teachub.common.utils.BeanUtils;
import com.teachub.user.domain.po.UserDetail;
import com.teachub.user.domain.query.UserPageQuery;
import com.teachub.user.domain.vo.StaffVO;
import com.teachub.user.service.IStaffService;
import com.teachub.user.service.IUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 员工详情表 服务实现类
 * </p>
 *
 *
 * @  07-12
 */
@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements IStaffService {

    private final IUserDetailService detailService;
    private final RoleCache roleCache;
    @Override
    public PageDTO<StaffVO> queryStaffPage(UserPageQuery query) {
        // 1.搜索
        Page<UserDetail> p = detailService.queryUserDetailByPage(query, UserType.STAFF);
        // 2.处理vo
        return PageDTO.of(p, u -> {
            StaffVO v = BeanUtils.toBean(u, StaffVO.class);
            v.setRoleName(roleCache.getRoleName(u.getRoleId()));
            return v;
        });
    }
}
