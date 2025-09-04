package com.touhouqing.crawllangai.service;

import com.touhouqing.crawllangai.model.mysql.Announcement;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author TouHouQing
 * @since 2025-09-01
 */
public interface AnnouncementService extends IService<Announcement> {


    /**
     * 提取并保存公告详情
     */
    void crawlerDetail() throws Exception;

    /**
     * 提取并保存中标详情
     */
    void crawlerBid() throws Exception;

    /**
     * 爬取公告
     */
    void crawlerTitle();
}
