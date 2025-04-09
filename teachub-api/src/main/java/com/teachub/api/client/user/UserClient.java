package com.teachub.api.client.user;

import com.teachub.api.dto.user.LoginFormDTO;
import com.teachub.api.dto.user.UserDTO;
import com.teachub.common.domain.dto.LoginUserDTO;
import java.util.List;

public interface UserClient {

    /**
     * 根据手机号查询用户id
     * @param phone 手机号
     * @return 用户id
     */
    Long exchangeUserIdWithPhone(String phone);

    /**
     * 登录接口
     * @param loginDTO 登录信息
     * @param isStaff 是否是员工
     * @return 用户详情
     */
    LoginUserDTO queryUserDetail(LoginFormDTO loginDTO, boolean isStaff);

    /**
     * 查询用户类型
     * @param id 用户id
     * @return 用户类型，0-普通学员，1-老师，2-其他员工
     */
    Integer queryUserType(Long id);

    /**
     * <h1>根据id批量查询用户信息</h1>
     * @param ids 用户id集合
     * @return 用户集合
     */
    List<UserDTO> queryUserByIds(Iterable<Long> ids);


    /**
     * 根据id查询单个学生信息
     * @param id 用户id
     * @return 学生
     */
    UserDTO queryUserById(Long id);
}
