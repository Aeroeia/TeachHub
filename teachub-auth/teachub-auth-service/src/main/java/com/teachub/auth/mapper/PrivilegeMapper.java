package com.teachub.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teachub.auth.domain.po.Privilege;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 权限表，包括菜单权限和访问路径权限 Mapper 接口
 * </p>
 *
 *
 * @  06-15
 */
public interface PrivilegeMapper extends BaseMapper<Privilege> {

    List<Privilege> listRolePrivileges(@Param("roleId") Long roleId);
}
