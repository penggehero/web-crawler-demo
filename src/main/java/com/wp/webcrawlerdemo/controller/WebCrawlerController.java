package com.wp.webcrawlerdemo.controller;

import com.github.pagehelper.PageInfo;
import com.wp.webcrawlerdemo.domain.entity.Course;
import com.wp.webcrawlerdemo.domain.vo.CourseCategoryTreeVo;
import com.wp.webcrawlerdemo.domain.vo.CourseVo;
import com.wp.webcrawlerdemo.service.WebCrawlerService;
import com.wp.webcrawlerdemo.task.WebCrawlerTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wupeng
 * @date 2021/4/26 9:17
 */
@RestController
@CrossOrigin
@RequestMapping("/crawler")
public class WebCrawlerController {
    @Autowired
    private WebCrawlerTask webCrawlerTask;

    @Autowired
    private WebCrawlerService webCrawlerService;

    @GetMapping("/test")
    public String test() {
        webCrawlerTask.getInfoFromKeQQ();
        return "sucess";
    }

    @GetMapping("/getTree")
    public List<CourseCategoryTreeVo> getCourseCategoryTreeVo(String date) throws Exception {
        List<CourseCategoryTreeVo> list = new ArrayList<>();
        list.add(webCrawlerService.getCourseCategoryTreeVo(date));
        return list;
    }

    @PostMapping("/getPage")
    public PageInfo<Course> getPage(@RequestBody CourseVo course) {
        return webCrawlerService.getPage(course);
    }


}
