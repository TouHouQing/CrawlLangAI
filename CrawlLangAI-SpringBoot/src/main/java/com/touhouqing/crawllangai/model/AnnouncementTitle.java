package com.touhouqing.crawllangai.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.io.Serial;
import java.time.LocalDate;
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
 * @since 2025-08-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("announcement_title")
public class AnnouncementTitle implements Serializable {

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
    private LocalDate releaseDate;

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


}
