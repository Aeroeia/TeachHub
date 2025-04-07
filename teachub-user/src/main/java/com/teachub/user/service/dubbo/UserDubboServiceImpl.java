package com.teachub.user.service.dubbo;

import com.teachub.api.client.user.UserClient;
import com.teachub.api.dto.user.LoginFormDTO;
import com.teachub.api.dto.user.UserDTO;
import com.teachub.common.domain.dto.LoginUserDTO;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.utils.BeanUtils;
import com.teachub.common.utils.CollUtils;
import com.teachub.user.constants.UserErrorInfo;
import com.teachub.user.domain.po.User;
import com.teachub.user.domain.po.UserDetail;
import com.teachub.user.service.IUserDetailService;
import com.teachub.user.service.IUserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@DubboService
public class UserDubboServiceImpl implements UserClient {

    @Autowired
    private IUserService userService;
    @Autowired
    private IUserDetailService detailService;

    @Override
    public Long exchangeUserIdWithPhone(String phone) {
        User user = userService
                .lambdaQuery().eq(User::getCellPhone, phone).one();
        if (user == null) {
            throw new BadRequestException(UserErrorInfo.Msg.USER_ID_NOT_EXISTS);
        }
        return user.getId();
    }

    @Override
    public LoginUserDTO queryUserDetail(LoginFormDTO loginDTO, boolean isStaff) {
        return userService.queryUserDetail(loginDTO, isStaff);
    }

    @Override
    public Integer queryUserType(Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new BadRequestException(UserErrorInfo.Msg.USER_ID_NOT_EXISTS);
        }
        return user.getType().getValue();
    }

    @Override
    public List<UserDTO> queryUserByIds(Iterable<Long> ids) {
        List<Long> idList = new ArrayList<>();
        if(ids != null) ids.forEach(idList::add);
        if(CollUtils.isEmpty(idList)){
            return CollUtils.emptyList();
        }
        List<UserDetail> list = detailService.queryByIds(idList);
        return BeanUtils.copyList(list, UserDTO.class, (d, u) -> u.setType(d.getType().getValue()));
    }

    @Override
    public UserDTO queryUserById(Long id) {
        UserDetail userDetail = detailService.queryById(id);
        return BeanUtils.copyBean(userDetail, UserDTO.class, (d, u) -> u.setType(d.getType().getValue()));
    }
}
