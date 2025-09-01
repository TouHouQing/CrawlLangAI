package com.touhouqing.crawllangai.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.io.Serial;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author TouHouQing
 * @since 2025-09-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("announcement_detail")
public class AnnouncementDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公告链接
     */
    @TableField("url")
    private String url;

    /**
     * 公告的发布日期
     */
    @TableField("release_date")
    private String releaseDate;

    /**
     * 公告标题
     */
    @TableField("title")
    private String title;

    /**
     * 项目编号
     */
    @TableField("project_number")
    private String projectNumber;

    /**
     * 发布来源
     */
    @TableField("publish_source")
    private String publishSource;

    /**
     * 预算金额，单位万元
     */
    @TableField("budget_amount")
    private String budgetAmount;

    /**
     * 采购需求描述
     */
    @TableField("purchase_requirement")
    private String purchaseRequirement;


}
