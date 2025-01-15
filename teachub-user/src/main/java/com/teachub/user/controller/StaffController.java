package com.teachub.user.controller;


import com.teachub.common.domain.dto.PageDTO;
import com.teachub.user.domain.query.UserPageQuery;
import com.teachub.user.domain.vo.StaffVO;
import com.teachub.user.service.IStaffService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 员工详情表 前端控制器
 * </p>
 *
 */
@RestController
@RequestMapping("/staffs")
@Api(tags = "用户管理接口")
public class StaffController {

    private final IStaffService staffService;

    public StaffController(IStaffService staffService) {
        this.staffService = staffService;
    }

    @ApiOperation("分页查询员工信息")
    @GetMapping("page")
    public PageDTO<StaffVO> queryStaffPage(UserPageQuery pageQuery){
        return staffService.queryStaffPage(pageQuery);
    }
}
