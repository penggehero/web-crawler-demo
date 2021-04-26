package com.wp.webcrawlerdemo.domain.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author wupeng
 * @date 2021/4/25 21:44
 */
@Data
public class CourseCategory {
    // id
    private Integer id;
    // 课程名称
    private String courseCategoryName;
    // 一级分类id （腾讯学堂官方）
    private Integer mt;
    // 二级分类id （腾讯学堂官方）
    private Integer st;
    // 三级分类id （腾讯学堂官方）
    private Integer tt;
    // 层级
    private Integer level;
    // 分类课程数
    private Integer courseNumber;
    // 爬取时间
    private Date date;
}
