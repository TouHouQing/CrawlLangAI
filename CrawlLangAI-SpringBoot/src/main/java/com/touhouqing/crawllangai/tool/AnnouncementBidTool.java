package com.touhouqing.crawllangai.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.touhouqing.crawllangai.mapper.AnnouncementMapper;
import com.touhouqing.crawllangai.model.Announcement;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnnouncementBidTool {

    private final AnnouncementMapper announcementMapper;
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
        Announcement announcement = announcementMapper.selectOne(new LambdaQueryWrapper<Announcement>().eq(Announcement::getProjectNumber, projectNumber));
        if (announcement == null) {
            return "保存成功";
        }
        //保存中标详情
        announcement.setBidCompany(bidCompany);
        announcement.setBidContent(bidContent);
        announcementMapper.updateById(announcement);
        return "保存成功";
    }
}
