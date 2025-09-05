package com.touhouqing.crawllangai.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.touhouqing.crawllangai.mapper.AnnouncementMapper;
import com.touhouqing.crawllangai.mapper.CompanyMapper;
import com.touhouqing.crawllangai.model.mysql.Announcement;
import com.touhouqing.crawllangai.model.mysql.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import com.touhouqing.crawllangai.service.ParticipantService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnnouncementBidTools {

    private final AnnouncementMapper announcementMapper;

    private final CompanyMapper companyMapper;

    private final ParticipantService participantService;
    /**
     * 保存中标详情
     */
    @Tool(name = "保存中标详情工具",description = "用于保存中标详情")
    public String saveAnnouncementBid(
            @ToolParam(description = "项目编号") String projectNumber,
            @ToolParam(description = """
                    中标信息描述，包含多个包号信息,格式要符合idea传入mysql的json格式示例，名称遇到符号需要转义如\\"项目需求书\\""：\
                    [{"包号": "1","供应商名称": "杭州奥创光子技术有限公司","供应商地址": "浙江省杭州市萧山区萧山经济技术开发区建设二路 858 号 D 幢二楼 216室","统一社会信用代码": "91330109MA2CCT4X1A","企业办公电话": "0571-82965130","中标金额(万元)": "56.9"}
                    ]""") String bidCompany,
            @ToolParam(description = """
                    主要标的信息描述，包含多个包号信息,格式要符合idea传入mysql的json格式示例，名称遇到符号需要转义如\\"项目需求书\\""：\
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
                    ]""") String bidContent,
            @ToolParam(description = """
                    招标联系信息，包含采购人信息，采购代理机构信息，项目联系方式等,格式要符合idea传入mysql的json格式示例，名称遇到符号需要转义如\\"项目需求书\\""：\
                    {
                    "采购人信息":[
                      {
                        "名称": "天津工业大学",
                        "地址": "天津市西青区宾水西道399号",
                        "联系方式": "022-83955042",
                      }
                    ],
                    "采购代理机构信息":[
                      {
                      }
                    ],
                    "项目联系方式":[
                      {
                      }
                    ]
                    }""") String tenderContact
    ){
        //根据项目编号查找招标详情
        Announcement announcement = announcementMapper.selectOne(new LambdaQueryWrapper<Announcement>().eq(Announcement::getProjectNumber, projectNumber));
        if (announcement == null) {
            return "保存失败，项目编号不存在，停止对话，不再需要调用此工具";
        }
        //保存中标详情
        announcement.setBidCompany(bidCompany);
        announcement.setBidContent(bidContent);
        announcement.setTenderContact(tenderContact);
        announcementMapper.updateById(announcement);
        //保存中标公司，先解析中标公司的json数据
        try {
            JSONArray jsonArray = new JSONArray(bidCompany);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject companyJson = jsonArray.getJSONObject(i);
                String socialCode = companyJson.getString("统一社会信用代码");
                
                //查询是否已存在
                Company company = companyMapper.selectOne(new LambdaQueryWrapper<Company>().eq(Company::getSocialCode, socialCode));
                if (company != null) {
                    //已存在，更新
                    company.setName(companyJson.getString("供应商名称"));
                    company.setAddress(companyJson.getString("供应商地址"));
                    company.setSocialCode(socialCode);
                    company.setPhone(companyJson.getString("企业办公电话"));
                    companyMapper.updateById(company);
                } else {
                    //不存在，插入
                    company = new Company();
                    company.setName(companyJson.getString("供应商名称"));
                    company.setAddress(companyJson.getString("供应商地址"));
                    company.setSocialCode(socialCode);
                    company.setPhone(companyJson.getString("企业办公电话"));

                    companyMapper.insert(company);
                }

                // 保存 Neo4j 节点与关系：招标者(来自 announcement.title/publish_source?) -> 中标者
                // 招标者名称从公告标题中一般不可直接解析，这里用发布来源作为近似招标者
                // 若后续有更准确字段，可替换 tendererName 的来源
                String tendererName = announcement.getPublishSource();
                String winnerName = companyJson.getString("供应商名称");
                if (tendererName != null && !tendererName.isEmpty()) {
                    participantService.createBidRelation(tendererName, winnerName);
                }
                // 保存 Neo4j 节点与关系：招标者 -> 采购代理机构
                // 采购代理机构名称从招标联系信息中解析
                JSONObject contactJson = new JSONObject(tenderContact);
                JSONArray proxyArray = contactJson.getJSONArray("采购代理机构信息");
                if (!proxyArray.isEmpty()) {
                    JSONObject proxyJson = proxyArray.getJSONObject(0);
                    String proxyName = proxyJson.getString("名称");
                    if (proxyName != null && !proxyName.isEmpty()) {
                        participantService.createProxyRelation(tendererName, proxyName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析中标公司JSON数据失败: {}", e.getMessage(), e);
            return "保存失败，停止对话，不再需要调用此工具 " + e.getMessage();
        }

        return "保存成功，停止对话，不再需要调用此工具";
    }
}
