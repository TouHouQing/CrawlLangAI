package com.touhouqing.crawllangai.service;

import com.touhouqing.crawllangai.model.AnnouncementTitle;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author TouHouQing
 * @since 2025-08-30
 */
public interface AnnouncementTitleService extends IService<AnnouncementTitle> {

    /**
     * 爬取公告
     */
    void crawler();
}
