package com.wp.webcrawlerdemo.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wp.webcrawlerdemo.domain.entity.Course;
import com.wp.webcrawlerdemo.domain.entity.CourseCategory;
import com.wp.webcrawlerdemo.domain.vo.CourseCategoryTreeVo;
import com.wp.webcrawlerdemo.domain.vo.CourseVo;
import com.wp.webcrawlerdemo.mapper.CourseCategoryMapper;
import com.wp.webcrawlerdemo.mapper.CourseMapper;
import com.wp.webcrawlerdemo.service.WebCrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author wupeng
 * @date 2021/4/25 21:51
 */
@Service
@Slf4j
public class WebCrawlerServiceImpl implements WebCrawlerService {
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    // 线程池提供异步操作
    private static ExecutorService executor = Executors.newFixedThreadPool(10);


    private static final String INDEX_URL = "https://ke.qq.com/";
    private static final String COURSE_URL = "https://ke.qq.com/course/list";

    @Override
    public void getInfoFromKeQQ() throws Exception {
        Date now = new Date();
        List<CourseCategory> allLevel = getAllLevel(now);
        if (!CollectionUtils.isEmpty(allLevel)) {
            for (CourseCategory courseCategory : allLevel) {
                // 三级分类下面的课程信息 使用多线程异步拉取
                executor.submit(() -> {
                    try {
                        getCourseInfo(courseCategory, now);
                    } catch (Exception e) {
                        log.error("爬取课程信息出现错误！",e);
                    }
                });
            }
        }
        summary(now);
    }


    @Override
    public CourseCategoryTreeVo getCourseCategoryTreeVo(String dateStr) throws Exception {
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        List<CourseCategory> courseCategories = courseCategoryMapper.queryByDate(date);
        CourseCategoryTreeVo courseCategoryTreeVo = new CourseCategoryTreeVo();
        courseCategoryTreeVo.setId(0);
        courseCategoryTreeVo.setLabel("所有分类");
        courseCategoryTreeVo.setCourseNumber(0);
        if (!CollectionUtils.isEmpty(courseCategories)) {
            int sum = courseCategories.stream().filter(o -> o.getLevel().equals(1)).mapToInt(CourseCategory::getCourseNumber).sum();
            courseCategoryTreeVo.setCourseNumber(sum);
            buildCourseCategoryTreeVo(courseCategories, courseCategoryTreeVo, 1);
        }
        return courseCategoryTreeVo;
    }

    @Override
    public PageInfo<Course> getPage(CourseVo courseVo) {
        PageHelper.startPage(courseVo.getPageNum(), courseVo.getPageSize());
        List<Course> courses = courseMapper.queryAll(courseVo);
        return new PageInfo<>(courses);
    }

    private void buildCourseCategoryTreeVo(List<CourseCategory> courseCategories, CourseCategoryTreeVo three, int level) {
        Predicate<CourseCategory> predicate = (CourseCategory o) -> {
            if (!Objects.isNull(three.getTt())) {
                return o.getLevel().equals(level) && Objects.equals(o.getMt(), three.getMt()) && Objects.equals(o.getSt(), three.getSt()) && Objects.equals(o.getTt(), three.getTt());
            }
            if (!Objects.isNull(three.getSt())) {
                return o.getLevel().equals(level) && Objects.equals(o.getMt(), three.getMt()) && Objects.equals(o.getSt(), three.getSt());
            }
            if (!Objects.isNull(three.getMt())) {
                return o.getLevel().equals(level) && Objects.equals(o.getMt(), three.getMt());
            }
            return o.getLevel().equals(level);
        };

        List<CourseCategoryTreeVo> child = courseCategories.stream().filter(predicate).map(
                o -> {
                    CourseCategoryTreeVo courseCategoryTreeVo = new CourseCategoryTreeVo();
                    courseCategoryTreeVo.setId(o.getId());
                    courseCategoryTreeVo.setLabel(o.getCourseCategoryName());
                    courseCategoryTreeVo.setCourseNumber(o.getCourseNumber());
                    courseCategoryTreeVo.setMt(o.getMt());
                    courseCategoryTreeVo.setSt(o.getSt());
                    courseCategoryTreeVo.setTt(o.getTt());
                    return courseCategoryTreeVo;
                }
        ).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(child)) {
            three.setChildren(child);
            for (CourseCategoryTreeVo courseCategoryTreeVo : child) {
                buildCourseCategoryTreeVo(courseCategories, courseCategoryTreeVo, level + 1);
            }
        }

    }


    @Override
    public void summary(Date date) {
        List<CourseCategory> courseCategories01 = courseCategoryMapper.queryByLevel(date, 1);
        List<CourseCategory> courseCategories02 = courseCategoryMapper.queryByLevel(date, 2);
        List<CourseCategory> courseCategories03 = courseCategoryMapper.queryByLevel(date, 3);
        summaryCourseCategory(courseCategories01);
        summaryCourseCategory(courseCategories02);
        summaryCourseCategory(courseCategories03);

    }

    private void summaryCourseCategory(List<CourseCategory> list) {
        if (!CollectionUtils.isEmpty(list)) {
            for (CourseCategory courseCategory : list) {
                Integer courseNumber = courseMapper.getCourseNumber(courseCategory);
                courseCategory.setCourseNumber(courseNumber);
                courseCategoryMapper.update(courseCategory);
            }
        }
    }


    @Override
    public void getCourseInfo(CourseCategory courseCategory, Date date) throws Exception {
        StringBuilder sb = new StringBuilder(COURSE_URL);
        if (courseCategory != null) {
            sb.append("?");
            if (!Objects.isNull(courseCategory.getMt())) {
                sb.append("mt=").append(courseCategory.getMt());
            }
            if (!Objects.isNull(courseCategory.getSt())) {
                sb.append("&st=").append(courseCategory.getSt());
            }
            if (!Objects.isNull(courseCategory.getTt())) {
                sb.append("&tt=").append(courseCategory.getTt());
            }
            sb.append("&page=");
        }
        String requestUrl = sb.toString();
        int page = 1;
        Integer pageCount = null;
        do {
            Document document = Jsoup.connect(requestUrl + page).get();
            Elements courseList = document.getElementsByClass("course-card-list").select("li");
            if (pageCount == null) {
                pageCount = getPageSize(document);
            }
            for (Element element : courseList) {
                Elements priceElements = element.getElementsByClass("line-cell item-price  custom-string");
                // 没有价格信息就不是要爬取的课程信息
                if (priceElements == null || StringUtils.isEmpty(priceElements.text())) {
                    continue;
                }
                Elements itemElements = element.getElementsByClass("item-tt");
                Course course = new Course();
                course.setDate(date);
                course.setSt(courseCategory.getSt());
                course.setMt(courseCategory.getMt());
                course.setTt(courseCategory.getTt());
                course.setCourseId(getCourseId(itemElements.select("a").attr("href")));
                course.setTitle(itemElements.text());
                course.setPrice(priceElements.text());
                course.setIsPackage(itemElements.select("a").attr("href").contains("package"));
                course.setTeacher(element.getElementsByClass("item-line item-line--middle").select("a").text());
                // 保存入库
                courseMapper.insert(course);
            }
            page++;
        } while (page <= pageCount);
    }

    /**
     * 爬取主页的所有分类并保存入库，返回所有的三级分类
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<CourseCategory> getAllLevel(Date date) throws Exception {
        // 保存所有的三级分类
        List<CourseCategory> courseCategoriesLevel = new ArrayList<>();
        Document document = Jsoup.connect(INDEX_URL).get();
        Elements allA = document.select("header").select("a");
        for (Element element : allA) {
            String href = element.attr("href");
            Map<String, String> urlParam = getUrlParam(href);
            Integer categoryLevel = getCategoryLevel(urlParam);
            // 如果链接是一个课程详情链接
            if (!categoryLevel.equals(-1)) {
                CourseCategory courseCategory = new CourseCategory();
                courseCategory.setCourseCategoryName(element.text());
                courseCategory.setLevel(categoryLevel);
                courseCategory.setDate(date);
                switch (categoryLevel) {
                    case 1:
                        // 1级分类
                        courseCategory.setMt(Integer.valueOf(urlParam.get("mt")));
                        break;
                    case 2:
                        // 2级分类
                        courseCategory.setMt(Integer.valueOf(urlParam.get("mt")));
                        courseCategory.setSt(Integer.valueOf(urlParam.get("st")));
                        break;
                    case 3:
                        //3级分类
                        courseCategory.setMt(Integer.valueOf(urlParam.get("mt")));
                        courseCategory.setSt(Integer.valueOf(urlParam.get("st")));
                        courseCategory.setTt(Integer.valueOf(urlParam.get("tt")));
                        courseCategoriesLevel.add(courseCategory);
                        break;

                }
                //保存入库
                courseCategoryMapper.insert(courseCategory);


            }
        }
        return courseCategoriesLevel;
    }

    private static Map<String, String> getUrlParam(String url) {
        if (!url.contains("?")) {
            return null;
        }
        String substring = url.substring(url.indexOf("?") + 1);
        Map<String, String> map = new HashMap<>();
        if (!StringUtils.isEmpty(substring)) {
            String[] split = substring.split("&");
            for (String str : split) {
                String[] keyAndValue = str.split("=");
                if (keyAndValue.length == 2) {
                    map.put(keyAndValue[0], keyAndValue[1]);
                }
            }
        }
        return map.isEmpty() ? null : map;
    }

    private static Integer getCategoryLevel(Map<String, String> urlParam) {
        if (urlParam == null) {
            return -1;
        }
        if (urlParam.containsKey("tt")) {
            return 3;
        }
        if (urlParam.containsKey("st")) {
            return 2;
        }
        if (urlParam.containsKey("mt")) {
            return 1;
        }
        return -1;
    }

    private Integer getCourseId(String url) {
        String[] split = url.split("/");
        return Integer.valueOf(split[split.length - 1]);
    }

    private Integer getPageSize(Document document) {
        Elements page = document.getElementsByClass("sort-page").select("a");
        if (page == null) {
            return 1;
        }
        for (int i = page.size() - 1; i >= 0; i--) {
            Element element = page.get(i);
            if ("page-next-btn icon-font i-v-right".equals(element.attr("class"))) {
                continue;
            } else {
                return Integer.valueOf(element.text());
            }
        }
        return 1;
    }
}
