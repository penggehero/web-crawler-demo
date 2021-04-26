package com.wp.webcrawlerdemo.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * @author wupeng
 * @date 2021/4/26 10:17
 */
@Data
public class CourseCategoryTreeVo {
    private Integer id;
    private String label;
    // 一级分类id （腾讯学堂官方）
    private Integer mt;
    // 二级分类id （腾讯学堂官方）
    private Integer st;
    // 三级分类id （腾讯学堂官方）
    private Integer tt;
    private Integer courseNumber;
    private List<CourseCategoryTreeVo> children;


}
