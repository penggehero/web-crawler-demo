package com.wp.webcrawlerdemo.domain.vo;

import com.wp.webcrawlerdemo.domain.entity.Course;
import lombok.Data;

/**
 * @author wupeng
 * @date 2021/4/26 11:40
 */
@Data
public class CourseVo extends Course {
    private Integer pageNum;
    private Integer pageSize;
}
