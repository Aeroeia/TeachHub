package com.teachub.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.auth.domain.po.RoleMenu;
import com.teachub.auth.mapper.RoleMenuMapper;
import com.teachub.auth.service.IRoleMenuService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 账户、角色关联表 服务实现类
 * </p>
 *
 *
 * @  06-16
 */
@Service
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, RoleMenu> implements IRoleMenuService {

    @Override
    public void removeByRoleId(Long id) {
        remove(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id));
    }

    @Override
    public void deleteRoleMenus(Long roleId, List<Long> menuIds) {
        // delete from role_menus where role_id = ? and menu_id in (?,?);
        remove(
                new LambdaQueryWrapper<RoleMenu>()
                        .eq(RoleMenu::getRoleId, roleId)
                        .in(RoleMenu::getMenuId, menuIds)
        );
    }
}
