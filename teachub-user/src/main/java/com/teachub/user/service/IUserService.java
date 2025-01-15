package com.teachub.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teachub.api.dto.user.LoginFormDTO;
import com.teachub.api.dto.user.UserDTO;
import com.teachub.common.domain.dto.LoginUserDTO;
import com.teachub.user.domain.dto.UserFormDTO;
import com.teachub.user.domain.po.User;
import com.teachub.user.domain.vo.UserDetailVO;

/**
 * <p>
 * 学员用户表 服务类
 * </p>
 *
 *
 * @  06-28
 */
public interface IUserService extends IService<User> {
    LoginUserDTO queryUserDetail(LoginFormDTO loginDTO, boolean isStaff);

    void resetPassword(Long userId);

    UserDetailVO myInfo();

    void addUserByPhone(User user, String code);

    void updatePasswordByPhone(String cellPhone, String code, String password);

    Long saveUser(UserDTO userDTO);

    void updateUser(UserDTO userDTO);

    void updateUserWithPassword(UserFormDTO userDTO);
}
