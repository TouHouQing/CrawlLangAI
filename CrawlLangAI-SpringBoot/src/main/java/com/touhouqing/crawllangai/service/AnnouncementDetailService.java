package com.touhouqing.crawllangai.service;

import com.touhouqing.crawllangai.model.AnnouncementDetail;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author TouHouQing
 * @since 2025-09-01
 */
public interface AnnouncementDetailService extends IService<AnnouncementDetail> {


    /**
     * 提取并保存公告详情
     */
    void crawler() throws Exception;
}
