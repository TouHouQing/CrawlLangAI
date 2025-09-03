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

import java.time.LocalDate;
@Component
@RequiredArgsConstructor
public class AnnounceDetailTool{

    private final AnnouncementDetailMapper announcementDetailMapper;

    private final AnnouncementTitleMapper announcementTitleMapper;

    @Tool(name = "保存公告详情数据工具",description ="用于保存公告详情数据" )
    public String saveAnnouncementDetail(
            @ToolParam(description = "项目标题") String title,
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
        //根据项目编号查找url
        AnnouncementTitle announcementTitle = announcementTitleMapper.selectOne(new LambdaQueryWrapper<AnnouncementTitle>().eq(AnnouncementTitle::getProjectNumber, projectNumber));
        if (announcementTitle == null) {
            return "保存成功";
        }
        AnnouncementDetail announcementDetail = new AnnouncementDetail().setTitle(title)
                .setBudgetAmount(budgetAmount)
                .setPurchaseRequirement(purchaseRequirement)
                .setProjectNumber(projectNumber)
                .setPublishSource(publishSource)
                .setReleaseDate(releaseDate)
                .setUrl(announcementTitle.getUrl());
        announcementDetailMapper.insert(announcementDetail);
        return "保存成功";
    }

    /**
     * 保存中标详情
     */
    @Tool(name = "保存中标详情工具",description = "用于保存中标详情")
    public String saveAnnouncementBid(
            @ToolParam(description = "项目编号") String projectNumber,
            @ToolParam(description = """
                    中标信息描述，包含多个包号信息,格式要符合idea传入mysql的json格式示例：\
                    [
                      {
                        "包号": "1",
                        "供应商名称": "杭州奥创光子技术有限公司",
                        "供应商地址": "浙江省杭州市萧山区萧山经济技术开发区建设二路 858 号 D 幢二楼 216室",
                        "统一社会信用代码": "91330109MA2CCT4X1A",
                        "企业办公电话": "0571-82965130",
                        "中标金额(万元)": "56.9"
                      }
                    ]""") String bidCompany,
             @ToolParam(description = """
                    主要标的信息描述，包含多个包号信息,格式要符合idea传入mysql的json格式示例：\
                    [
                      {
                        "包号": "1",
                        "类型": "货物类",
                        "名称": "高重频飞秒激光放大器",
                        "品牌": "奥创光子",
                        "规格型号": "FL-20-IR",
                        "数量": "1",
                        "单价(万元)": "56.9"
                      }
                    ]""") String bidContent
    ){
        //根据项目编号查找招标详情
        AnnouncementDetail announcementDetail = announcementDetailMapper.selectOne(new LambdaQueryWrapper<AnnouncementDetail>().eq(AnnouncementDetail::getProjectNumber, projectNumber));
        if (announcementDetail == null) {
            return "保存成功";
        }
        //保存中标详情
        announcementDetail.setBidCompany(bidCompany);
        announcementDetail.setBidContent(bidContent);
        announcementDetailMapper.updateById(announcementDetail);
        return "保存成功";
    }
}
