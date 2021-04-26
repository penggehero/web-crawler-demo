package com.wp.webcrawlerdemo.service;

import com.github.pagehelper.PageInfo;
import com.wp.webcrawlerdemo.domain.entity.Course;
import com.wp.webcrawlerdemo.domain.entity.CourseCategory;
import com.wp.webcrawlerdemo.domain.vo.CourseCategoryTreeVo;
import com.wp.webcrawlerdemo.domain.vo.CourseVo;

import java.util.Date;
import java.util.List;

/**
 * @author wupeng
 * @date 2021/4/25 21:51
 */
public interface WebCrawlerService {
    public void getInfoFromKeQQ() throws Exception;

    public List<CourseCategory> getAllLevel(Date date) throws Exception;

    public void getCourseInfo(CourseCategory courseCategory,Date date) throws Exception;

    public void summary(Date date);

    CourseCategoryTreeVo getCourseCategoryTreeVo(String dateStr)  throws Exception ;

    PageInfo<Course> getPage(CourseVo courseVo);
}
