package com.teachub.auth.task;

import cn.hutool.core.collection.CollectionUtil;
import com.teachub.auth.common.domain.PrivilegeRoleDTO;
import com.teachub.auth.service.IPrivilegeService;
import com.teachub.auth.util.PrivilegeCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class LoadPrivilegeRunner{

    private final IPrivilegeService privilegeService;
    private final PrivilegeCache privilegeCache;

    @PostConstruct
    public void loadPrivilegeCache(){
        try {
            log.trace("开始更新权限缓存数据");
            // 1.查询数据
            List<PrivilegeRoleDTO> privilegeRoleDTOS = privilegeService.listPrivilegeRoles();
            if (CollectionUtil.isEmpty(privilegeRoleDTOS)) {
                return;
            }
            // 2.缓存
            privilegeCache.initPrivilegesCache(privilegeRoleDTOS);
            log.trace("更新权限缓存数据成功！");
        }catch (Exception e){
            log.error("更新权限缓存数据失败！原因：{}", e.getMessage());
        }
    }
}
