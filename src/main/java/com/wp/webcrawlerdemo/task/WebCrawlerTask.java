package com.wp.webcrawlerdemo.task;

import com.wp.webcrawlerdemo.service.WebCrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author wupeng
 * @date 2021/4/25 21:49
 */
@Component
@Slf4j
public class WebCrawlerTask {
    @Autowired
    private WebCrawlerService webCrawlerService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void getInfoFromKeQQ() {
        try {
            long start = System.currentTimeMillis();
            webCrawlerService.getInfoFromKeQQ();
            long end = System.currentTimeMillis();
            log.info("爬取过程总耗时为" + (end - start) + "毫秒");
        } catch (Exception e) {
            log.error("爬取过程中出现异常：", e);
        }
    }
}
