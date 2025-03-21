package com.teachub.promotion.controller;


import com.teachub.common.domain.dto.PageDTO;
import com.teachub.promotion.domain.dto.CodeQuery;
import com.teachub.promotion.domain.vo.CodeVO;
import com.teachub.promotion.service.IExchangeCodeService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 兑换码 前端控制器
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-30
 */
@RestController
@RequestMapping("/codes")
@Slf4j
@RequiredArgsConstructor
public class ExchangeCodeController {
    private final IExchangeCodeService exchangeCodeService;
    @ApiOperation("查看兑换码")
    @GetMapping("/page")
    public PageDTO<CodeVO> queryCodesPage(CodeQuery query) {
        log.info("查看兑换码");
        return exchangeCodeService.queryCodesPage(query);
    }
}

