package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptcha;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    //the funtion which is when user clicked "注册" in home page,we should direct to /register page
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String accessRegisterPage() {
        return "/site/register";
    }

    //get the login page method
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String accessLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    //只要页面上传入的值和User的属性相匹配，SpringMVC就会自动把值注入进user
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            //此时说明注册成功
            //成功后，页面跳转到首页（第三方页面：operate-result.html，显示页面在多少秒后跳转（转到首页））
            //激活后才跳转到登陆页面
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");//成功后跳转首页
            //此处返回这个中间界面的路径
            return "/site/operate-result";

        } else {
            //注册失败，把失败的信息，也就是Map里的Msg返回
            //key是字符串
            //有的结果可能是空的，直接把可能的信息都返回
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            //return to the register page
            return "/site/register";
        }
    }

    //http://localhost:15213/community/activation/101/code
    @RequestMapping(path = "/activation/{userID}/{code}", method = RequestMethod.GET)
    //@PathVariable annotation means get variables from path
    //激活成功应该跳转到operate-result页面.不同的处理结果给operate-result不同的target
    public String activation(Model model, @PathVariable("userID") int userId, @PathVariable("code") String code) {
        int result = userService.activate(userId, code);
        if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        } else {
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";

    }

    //verification code method
    //return a image to the page
    //输出图片到浏览器需要使用response，验证码是一个敏感文件，存在Cookie会有敏感信息的安全问题
    //所以存储到session里，之后会使用redis重构
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        //生成验证码，用Spring管理的容器注入配置
        //首先设置文本，然后把文本传入，创建验证码Image
        String text = kaptcha.createText();
        BufferedImage image = kaptcha.createImage(text);
        //将文本验证码存入session
        session.setAttribute("kaptcha", text);
        //将图片直接输出到浏览器
        //首先设置response返回内容的类型
        response.setContentType("image.png");
        try {
            ServletOutputStream os = response.getOutputStream();
            //输出图片的工具
            //三个参数：1.要输出的图片 2.格式 3.使用的流
            ImageIO.write(image, "png", os);
            //response是由SpringMVC管理的，不需要自己手动关闭os流
        } catch (IOException e) {
            //出现异常，输出日志
            logger.error("响应验证码失败:" + e.getMessage());
        }

    }

    /**
     * @param username   用户名
     * @param password   密码
     * @param code       验证码
     * @param rememberMe 是否勾选记住我，决定了凭证的过期时间·长短
     * @param model      model封装保存
     * @param session    之前用session记录保存的验证码，此处做一个校验
     * @param response   返回的ticket要存到cookie里，需要使用response
     * @return 返回路径
     */
    //上面也有一个请求方法Request Mapping是”/login“,是可以重复的，只要请求方法不同
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model, HttpSession session, HttpServletResponse response) {
        //首先判断验证码
        //先拿到之前保存的验证码
        String kaptcha = (String) session.getAttribute("kaptcha");
        //判断空值并忽略大小写比较，如果有任何不满足则验证码不对，依旧返回登陆页面
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            //给页面返回的提示放在model里
            model.addAttribute("codeMsg", "验证码不正确");
            return "/site/login";
        }
        //验证账号和密码
        //定义两个常量比较方便
        long expiredSeconds = rememberMe ? REMEMBER_ME_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> loginMap = userService.login(username, password, expiredSeconds);
        //根据是否有ticket判断成功还是失败
        if (loginMap.containsKey("ticket")) {
            //成功:把凭证通过cookie发送到客户端
            Cookie cookie = new Cookie("ticket", loginMap.get("ticket").toString());
            cookie.setPath(contextPath);//cookie都要设置访问路径，此处设置成动态的全项可访问
            cookie.setMaxAge((int) expiredSeconds);//设置cookie过期时间
            response.addCookie(cookie);//使用response发生送到客户端
            //返回重定向到首页
            /*关于此处为什么要重定向：
            当前的请求是登录, 却给浏览器返回了首页的模板, 很让人困惑的.
            重定向是重新发起了一个请求, 浏览器的地址发生了改变, 逻辑是严谨的.
            转发解决的的一次请求内部的跳转，重定向解决的是2次请求之间的跳转。
            请求就是指浏览器向服务器发起的一次访问，包括4个环节，建立连接、发送请求、接收请求、关闭连接。
            转发就是浏览器向服务器发了一次请求，服务器通过controller处理，但是它没处理完，
            将请求交给另外一个组件处理，还是同一个请求，没有跳出服务端，最后由第二个组件给浏览器做响应。
            二次请求，就是浏览器向服务器发出一次请求，服务器某组件将其完整处理完了，
            给浏览器一个响应，并建议浏览器访问另外的组件，以刷新页面的内容。浏览器发出第二次请求，是独立的，和第一次无关的请求。*/
            return "redirect:/index";

        } else {
            //失败
            model.addAttribute("usernameMsg", loginMap.get("usernameMsg"));
            model.addAttribute("passwordMsg", loginMap.get("passwordMsg"));

            return "/site/login";
        }


    }
    //该方法需要得到浏览器之前保存的cookie
    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    //此处通过@CookieValue注解让SpringMVC把cookie的值注入进来
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        //请求结束后，重定向到login界面
        return "redirect:/login";//默认会走GET请求的login
    }



}
