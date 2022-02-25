package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {
    @Autowired
    private MailClient mailClient;
    @Test
    public void testTextMail(){
        mailClient.sendMail("wanax0548@gmail.com","Test2","Welcome2");
    }

    //正常在Controller层调用template啥的，SpringMVC的DispatcherServlet会帮助我们做到
    //此处可以自己实例化一个模板引擎,该模板引擎来自thymeleaf
    @Autowired
    private TemplateEngine templateEngine;
    @Test
    public void testHtmlMail(){
        //给模板传参使用Context对象
        //import org.thymeleaf.context.Context;
        Context context = new Context();
        context.setVariable("username","小白兔的亲亲");
        String content = templateEngine.process("/mail/demo", context);
        mailClient.sendMail("@qq.com","Knock！Knock！...",content);
    }
}
