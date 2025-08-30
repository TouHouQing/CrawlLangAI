package com.touhouqing.crawllangai.model.vo;

import com.xxl.crawler.annotation.PageFieldSelect;
import com.xxl.crawler.annotation.PageSelect;
import com.xxl.crawler.constant.Const.SelectType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@PageSelect(cssQuery = "ul#div_ul_1 li")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AnnouncementTitleCrawlVo {
    /**
     * 公告标题：提取<a>标签的文本内容（SelectType.TEXT对应jQuery的.text()）
     */
    @PageFieldSelect(
            cssQuery = "a", // <a>标签包含标题文本
            selectType = SelectType.TEXT // 数据抽取方式：获取文本
    )
    private String title;

    /**
     * 公告链接：提取<a>标签的href属性（SelectType.ATTR对应jQuery的.attr()）
     */
    @PageFieldSelect(
            cssQuery = "a", // <a>标签包含链接
            selectType = SelectType.ATTR, // 数据抽取方式：获取属性
            selectVal = "href" // 属性名：href（官网说明：SelectType=ATTR时selectVal有效）
    )
    private String url;

    /**
     * 发布日期：提取<span class="time">的文本内容
     */
    @PageFieldSelect(
            cssQuery = ".time", // 日期在class="time"的<span>标签中
            selectType = SelectType.TEXT
    )
    private String releaseDate;
}