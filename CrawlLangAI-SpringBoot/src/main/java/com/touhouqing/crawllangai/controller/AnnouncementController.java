package com.touhouqing.crawllangai.controller;

import com.touhouqing.crawllangai.common.Result;
import com.touhouqing.crawllangai.service.AnnouncementService;
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
@RequestMapping("/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 提取并保存公告详情
     */
    @GetMapping("/detail")
    public Result announcementDetail() throws Exception {
        announcementService.crawlerDetail();
        return Result.success();
    }

    /**
     * 提取并保存中标详情
     */
    @GetMapping("/bid")
    public Result announcementBid() throws Exception {
        announcementService.crawlerBid();
        return Result.success();
    }

    /**
     * 爬取公告
     */
    @GetMapping("/title")
    public Result crawler() {
        announcementService.crawlerTitle();
        return Result.success();
    }

}