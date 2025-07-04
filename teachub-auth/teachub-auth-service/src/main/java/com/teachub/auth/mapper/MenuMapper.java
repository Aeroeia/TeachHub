package com.teachub.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teachub.auth.domain.po.Menu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 权限表，包括菜单权限和访问路径权限 Mapper 接口
 * </p>
 *
 *   
 * @  06-16
 */
public interface MenuMapper extends BaseMapper<Menu> {

    List<Menu> listByRoles(@Param("roleIds") List<Long> roleIds);
}
