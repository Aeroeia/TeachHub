package com.teachub.auth.service.dubbo;

import cn.hutool.core.collection.CollectionUtil;
import com.teachub.api.client.auth.AuthClient;
import com.teachub.api.dto.auth.RoleDTO;
import com.teachub.auth.domain.po.Role;
import com.teachub.auth.service.IRoleService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@DubboService
public class AuthDubboServiceImpl implements AuthClient {

    @Autowired
    private IRoleService roleService;

    @Override
    public RoleDTO queryRoleById(Long id) {
        Role role = roleService.getById(id);
        if (role == null) {
            return null;
        }
        return role.toDTO();
    }

    @Override
    public List<RoleDTO> listAllRoles() {
        List<Role> list = roleService.list();
        if (CollectionUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(Role::toDTO).collect(Collectors.toList());
    }
}
