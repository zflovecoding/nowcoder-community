package com.nowcoder.community;


import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

//we uses log framework is LogBack:https://logback.qos.ch/
//so we should import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
/*
引用博客：
之前公司使用的日志框架还是log4j，大约从16年中到现在，
不管是我参与的别人已经搭建好的项目还是我自己主导的项目，日志框架基本都换成了logback，

总结一下，logback大约有以下的一些优点：
- 内核重写、测试充分、初始化内存加载更小，这一切让logback性能和log4j相比有诸多倍的提升
- logback非常自然地直接实现了slf4j，这个严格来说算不上优点，只是这样，再理解slf4j的前提下会很容易理解logback，也同时很容易用其他日志框架替换logback
- logback有比较齐全的200多页的文档
- logback当配置文件修改了，支持自动重新加载配置文件，扫描过程快且安全，它并不需要另外创建一个扫描线程
- 支持自动去除旧的日志文件，可以控制已经产生日志文件的最大数量
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class LoggerTest {
    //Logger is
    private static final Logger logger= LoggerFactory.getLogger(LoggerTest.class);
    @Test
    public void testLogger(){
        System.out.println(logger.getName());

        logger.debug("debug log");
        logger.info("info log");
        logger.warn("warn log");
        logger.error("error log");
    }
}
