package com.wp.webcrawlerdemo.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author wupeng
 * @date 2021/4/25 21:47
 */
@Data
public class Course {
    // id（数据库id）
    private Integer id;
    // 课程id（腾讯学堂官方）
    private Integer courseId;
    // 课程标题
    private String title;
    // 一级分类id （腾讯学堂官方）
    private Integer mt;
    // 二级分类id （腾讯学堂官方）
    private Integer st;
    // 三级分类id （腾讯学堂官方）
    private Integer tt;
    // Teacher
    private String teacher;
    // 价格
    private String price;
    // 是否是合集类型
    private Boolean isPackage;
    // 爬取时间
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;
}
