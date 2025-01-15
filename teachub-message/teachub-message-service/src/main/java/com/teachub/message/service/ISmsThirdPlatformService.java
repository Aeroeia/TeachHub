package com.teachub.message.service;

import com.teachub.message.domain.dto.SmsThirdPlatformDTO;
import com.teachub.message.domain.dto.SmsThirdPlatformFormDTO;
import com.teachub.message.domain.query.SmsThirdPlatformPageQuery;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.message.domain.po.SmsThirdPlatform;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 第三方云通讯平台 服务类
 * </p>
 *
 *
 * @  08-19
 */
public interface ISmsThirdPlatformService extends IService<SmsThirdPlatform> {

    List<SmsThirdPlatform> queryAllPlatform();

    Long saveSmsThirdPlatform(SmsThirdPlatformFormDTO thirdPlatformDTO);

    void updateSmsThirdPlatform(SmsThirdPlatformFormDTO thirdPlatformDTO);

    PageDTO<SmsThirdPlatformDTO> querySmsThirdPlatforms(SmsThirdPlatformPageQuery query);

    SmsThirdPlatformDTO querySmsThirdPlatform(Long id);
}
