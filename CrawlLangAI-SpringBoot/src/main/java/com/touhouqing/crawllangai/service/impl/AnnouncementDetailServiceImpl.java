package com.touhouqing.crawllangai.service.impl;

import com.touhouqing.crawllangai.model.AnnouncementDetail;
import com.touhouqing.crawllangai.mapper.AnnouncementDetailMapper;
import com.touhouqing.crawllangai.service.AnnouncementDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.io.IOException;


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
public class AnnouncementDetailServiceImpl extends ServiceImpl<AnnouncementDetailMapper, AnnouncementDetail> implements AnnouncementDetailService {

    private final ChatClient aiClient;

    @Override
    public void crawler() throws Exception {
        String content = getWebPageAsMarkdown("http://www.ccgp-tianjin.gov.cn/viewer.do?id=776538025&ver=2");
        String result = aiClient.prompt()
                .user(content)
                .call()
                .content();
        System.out.println(result);
    }

    // 获取网页内容并转换为Markdown
    public String getWebPageAsMarkdown(String url) throws Exception {
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