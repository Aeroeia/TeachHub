package com.teachub.api.client.auth;

import com.teachub.api.dto.auth.RoleDTO;
import java.util.List;

public interface AuthClient {

    RoleDTO queryRoleById(Long id);

    List<RoleDTO> listAllRoles();
}
