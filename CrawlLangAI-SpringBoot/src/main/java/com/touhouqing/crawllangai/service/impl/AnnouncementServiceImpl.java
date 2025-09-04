package com.touhouqing.crawllangai.service.impl;

import com.touhouqing.crawllangai.model.mysql.Announcement;
import com.touhouqing.crawllangai.mapper.AnnouncementMapper;
import com.touhouqing.crawllangai.model.mysql.vo.AnnouncementTitleCrawlVo;
import com.touhouqing.crawllangai.service.AnnouncementService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.touhouqing.crawllangai.tool.AnnouncementBidTools;
import com.touhouqing.crawllangai.tool.AnnouncementDetailTools;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.xxl.crawler.XxlCrawler;
import com.xxl.crawler.pageloader.param.Response;
import com.xxl.crawler.pageparser.PageParser;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author TouHouQing
 * @since 2025-09-01
 */
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    private final ChatClient aiClient;

    private final AnnouncementMapper announcementMapper;

    private final AnnouncementDetailTools announcementDetailTools;

    private final AnnouncementBidTools announcementBidTools;

    /**
     * 提取并保存公告详情
     */
    @Override
    public void crawlerDetail() throws Exception {
        String content = getWebPageAsMarkdown("http://www.ccgp-tianjin.gov.cn/portal/documentView.do?method=view&id=761234201&ver=2");
        String result = aiClient.prompt()
                .system("用户会给你提供一个markdown文本,你需要提取公告的详情信息,并保存到数据库中,不需要回答任何内容,只允许保存一次")
                .user(content)
                .tools(announcementDetailTools)
                .call()
                .content();
        System.out.println(result);
    }

    /**
     * 提取并保存中标详情
     */
    @Override
    public void crawlerBid() throws Exception {
        String content = getWebPageAsMarkdown("http://www.ccgp-tianjin.gov.cn/portal/documentView.do?method=view&id=775738282&ver=2");
        String result = aiClient.prompt()
                .system("用户会给你提供一个markdown文本，你需要提取中标公告的中标信息和主要标的信息等信息保存到数据库中,不需要回答任何内容,只允许保存一次")
                .user(content)
                .tools(announcementBidTools)
                .call()
                .content();
        System.out.println(result);
    }

    @Override
    public void crawlerTitle() {
        // 1. 配置爬虫（官网Builder模式，链式设置参数）
        XxlCrawler crawler = new XxlCrawler.Builder()
                // 核心：爬虫入口URL（目标页面）
                .setUrls("http://www.ccgp-tianjin.gov.cn/portal/topicView.do?method=view&view=Infor&id=1665&ver=2&st=1")
                // URL白名单：限制扩散范围（本场景仅爬取当前页面，无需扩散，可设为空）
                .setWhiteUrlRegexs()
                // 线程数：官网推荐3-5线程，避免高频请求被拦截
                .setThreadCount(3)
                // 主动停顿：爬虫处理完页面后的停顿时间（官网强调：防反爬，单位毫秒）
                .setPauseMillis(15*1000)
                .setAllowSpread(false)
                // 超时时间：请求超时控制（官网推荐5000毫秒）
                .setTimeoutMillis(5000)
                // User-Agent：模拟浏览器（官网建议设置，避免被识别为爬虫）
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
                // 页面解析器：绑定TenderPageVO，处理爬取结果（官网核心接口）
                .setPageParser(new PageParser<AnnouncementTitleCrawlVo>() {
                    /**
                     * 解析后回调（官网说明：实时处理爬取数据，避免大对象堆积）
                     * @param response 包含爬取的PageVO列表
                     */
                    @Override
                    public void afterParse(Response<AnnouncementTitleCrawlVo> response) {
                        // 3. 处理爬取结果
                        if (response.getParseVoList() != null && !response.getParseVoList().isEmpty()) {
                            System.out.println("===== 爬取到 " + response.getParseVoList().size() + " 条公告 =====");
                            for (AnnouncementTitleCrawlVo tender : response.getParseVoList()) {
                                // 补全相对URL（官网未强制，但业务需处理：将/href转为绝对路径）
                                if (tender.getUrl() != null && !tender.getUrl().startsWith("http")) {
                                    tender.setUrl("http://www.ccgp-tianjin.gov.cn" + tender.getUrl());
                                }
                                // 将vo转换成实体类
                                Announcement announcement = new Announcement();
                                BeanUtils.copyProperties(tender, announcement);

                                // 从title中提取项目编号并设置到实体类的projectNumber字段中
                                String title = tender.getTitle();
                                if (title != null && title.contains("项目编号:")) {
                                    int start = title.indexOf("项目编号:") + 5;
                                    int end = title.indexOf(")", start);
                                    if (end != -1) {
                                        String projectNumber = title.substring(start, end);
                                        announcement.setProjectNumber(projectNumber);
                                    }
                                }

                                // 将String类型的releaseDate转换为LocalDate类型
                                String releaseDateString = tender.getReleaseDate();
                                if (releaseDateString != null && !releaseDateString.isEmpty()) {
                                    try {
                                        // 日期格式为 "EEE MMM dd HH:mm:ss zzz yyyy"
                                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", java.util.Locale.US);
                                        java.util.Date date = sdf.parse(releaseDateString);
                                        java.time.LocalDate releaseDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                                        announcement.setReleaseDate(releaseDate);
                                    } catch (Exception e) {
                                        System.err.println("日期转换失败: " + releaseDateString + " 错误: " + e.getMessage());
                                    }
                                }
                                //url如果不存在就保存到数据库
                                if(announcementMapper.selectByMap(Collections.singletonMap("url", announcement.getUrl())) == null){
                                    announcementMapper.insert(announcement);
                                }
                            }
                        } else {
                            System.out.println("未爬取到公告数据，请检查CSS选择器是否匹配页面结构");
                        }
                    }
                })
                // 页面加载器：本场景为静态页面，使用官网默认的JsoupPageLoader（速度快）
                // 若为动态页面，可切换为SeleniumChromePageLoader：.setPageLoader(new SeleniumChromePageLoader())
                .build();

        // 2. 启动爬虫（官网支持同步/异步：入参true=同步，false=异步）
        crawler.start(true); // 同步启动，爬虫结束后主线程才退出

    }


    // 获取网页内容并转换为Markdown
    private String getWebPageAsMarkdown(String url) throws Exception {
        // 1. 使用Jsoup爬取网页HTML
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)") // 模拟浏览器
                .timeout(10000) // 超时时间
                .get();

        // 2. 提取需要转换的HTML部分（如<body>标签内的内容）
        String htmlContent = doc.body().html();

        // 3. 使用Flexmark将HTML转换为Markdown
        FlexmarkHtmlConverter converter = FlexmarkHtmlConverter.builder().build();
        return converter.convert(htmlContent);
    }
}