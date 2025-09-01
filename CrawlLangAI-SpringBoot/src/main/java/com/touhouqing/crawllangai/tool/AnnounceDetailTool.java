package com.touhouqing.crawllangai.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.touhouqing.crawllangai.mapper.AnnouncementDetailMapper;
import com.touhouqing.crawllangai.mapper.AnnouncementTitleMapper;
import com.touhouqing.crawllangai.model.AnnouncementDetail;
import com.touhouqing.crawllangai.model.AnnouncementTitle;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnnounceDetailTool{

    private final AnnouncementDetailMapper announcementDetailMapper;

    private final AnnouncementTitleMapper announcementTitleMapper;

    @Tool(name = "保存公告详情数据工具",description ="用于保存公告详情数据" )
    public void getAnnounceDetail(
            @ToolParam(description = "项目标题") String title,
            @ToolParam(description = "发布时间") String releaseDate,
            @ToolParam(description = "发布来源") String publishSource,
            @ToolParam(description = "项目编号") String projectNumber,
            @ToolParam(description = "预算金额") String budgetAmount,
            @ToolParam(description = "采购需求描述") String purchaseRequirement
    ) {
        //根据项目编号查找url
        AnnouncementTitle announcementTitle = announcementTitleMapper.selectOne(new LambdaQueryWrapper<AnnouncementTitle>().eq(AnnouncementTitle::getProjectNumber, projectNumber));
        AnnouncementDetail announcementDetail = new AnnouncementDetail().setTitle(title)
                .setBudgetAmount(budgetAmount)
                .setPurchaseRequirement(purchaseRequirement)
                .setProjectNumber(projectNumber)
                .setPublishSource(publishSource)
                .setReleaseDate(releaseDate)
                .setUrl(announcementTitle.getUrl());
        announcementDetailMapper.insert(announcementDetail);
    }
}
