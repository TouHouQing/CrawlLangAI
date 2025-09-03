package com.touhouqing.crawllangai.controller;

import com.touhouqing.crawllangai.common.Result;
import com.touhouqing.crawllangai.service.AnnouncementDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author TouHouQing
 * @since 2025-09-01
 */
@RestController
@RequestMapping("/announcement-detail")
@RequiredArgsConstructor
public class AnnouncementDetailController {

    private final AnnouncementDetailService announcementDetailService;

    /**
     * 提取并保存公告详情
     */
    @GetMapping
    public Result announcementDetail() throws Exception {
        announcementDetailService.crawler();
        return Result.success();
    }

    /**
     * 提取并保存中标详情
     */
    @GetMapping("/bid")
    public Result announcementBid() throws Exception {
        announcementDetailService.crawlerBid();
        return Result.success();
    }

}