package com.touhouqing.crawllangai.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.touhouqing.crawllangai.mapper.AnnouncementMapper;
import com.touhouqing.crawllangai.model.Announcement;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
@Component
@RequiredArgsConstructor
public class AnnouncementDetailTool {

    private final AnnouncementMapper announcementMapper;

    @Tool(name = "保存公告详情数据工具",description ="用于保存公告详情数据" )
    public String saveAnnouncementDetail(
            @ToolParam(description = "发布时间，必须遵循date类型格式：yyyy-MM-dd") LocalDate releaseDate,
            @ToolParam(description = "发布来源") String publishSource,
            @ToolParam(description = "项目编号") String projectNumber,
            @ToolParam(description = "预算金额") String budgetAmount,
            @ToolParam(description = """
                    采购需求描述，包含多个包号信息,格式要符合idea传入mysql的json格式示例：\
                    [
                      {
                        "包号": "1",
                        "是否设置最高限价": "否",
                        "预算（万元）": "57",
                        "最高限价（万元）": "57",
                        "采购目录": "教学仪器",
                        "采购需求": "天津工业大学高重频飞秒激光放大器等设备购置项目（第一包：高重频飞秒激光放大器），具体内容详见\\"项目需求书\\""
                      }
                    ]""") String purchaseRequirement
    ) {
        //根据项目编号拿到对象
        Announcement announcement = announcementMapper.selectOne(new LambdaQueryWrapper<Announcement>().eq(Announcement::getProjectNumber, projectNumber));
        if (announcement == null) {
            return "保存失败，项目编号不存在，停止对话，不再需要调用此工具";
        }
        announcement.setBudgetAmount(budgetAmount)
                .setPurchaseRequirement(purchaseRequirement)
                .setProjectNumber(projectNumber)
                .setPublishSource(publishSource)
                .setReleaseDate(releaseDate);
        announcementMapper.updateById(announcement);
        return "保存成功，停止对话，不再需要调用此工具";
    }
}
