package com.touhouqing.crawllangai.controller;

import com.touhouqing.crawllangai.common.Result;
import com.touhouqing.crawllangai.service.AnnouncementTitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * @author TouHouQing
 * @since 2025-08-30
 */
@RestController
@RequestMapping("/announcement-title")
@RequiredArgsConstructor
public class AnnouncementTitleController {

    private final AnnouncementTitleService announcementTitleService;

    @GetMapping
    public Result crawler() {
        announcementTitleService.crawler();
        return Result.success();
    }
}
