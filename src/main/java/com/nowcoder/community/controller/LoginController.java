package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.config.KaptchaConfig;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
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

    //the funtion which is when user clicked "注册" in home page,we should direct to /register page
    @RequestMapping(path="/register",method = RequestMethod.GET)
    public String accessRegisterPage(){
        return "/site/register";
    }
    //get the login page method
    @RequestMapping("/login")
    public String accessLoginPage(){
        return "/site/login";
    }
    @RequestMapping(path="/register",method = RequestMethod.POST)
    //只要页面上传入的值和User的属性相匹配，SpringMVC就会自动把值注入进user
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if(map==null || map.isEmpty()){
            //此时说明注册成功
            //成功后，页面跳转到首页（第三方页面：operate-result.html，显示页面在多少秒后跳转（转到首页））
            //激活后才跳转到登陆页面
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target","/index");//成功后跳转首页
            //此处返回这个中间界面的路径
            return "/site/operate-result";

        }else{
            //注册失败，把失败的信息，也就是Map里的Msg返回
            //key是字符串
            //有的结果可能是空的，直接把可能的信息都返回
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            //return to the register page
            return "/site/register";
        }
    }
    //http://localhost:15213/community/activation/101/code
    @RequestMapping(path = "/activation/{userID}/{code}",method = RequestMethod.GET)
    //@PathVariable annotation means get variables from path
    //激活成功应该跳转到operate-result页面.不同的处理结果给operate-result不同的target
    public String activation(Model model ,@PathVariable("userID") int userId,@PathVariable("code") String code){
        int result = userService.activate(userId, code);
        if(result==ACTIVATION_REPEAT){
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("target", "/index");
        }else if(result==ACTIVATION_SUCCESS){
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        }else {
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";

    }

    //verification code method
    //return a image to the page
    @RequestMapping(path="/kaptcha",method = RequestMethod.GET)
    //输出图片到浏览器需要使用response，验证码是一个敏感文件，存在Cookie会有敏感信息的安全问题
    //所以存储到session里，之后会使用redis重构

    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //生成验证码，用Spring管理的容器注入配置
        //首先设置文本，然后把文本传入，创建验证码Image
        String text = kaptcha.createText();
        BufferedImage image = kaptcha.createImage(text);
        //将文本验证码存入session
        session.setAttribute("kaptcha",text);
        //将图片直接输出到浏览器
        //首先设置response返回内容的类型
        response.setContentType("image.png");
        try {
            ServletOutputStream os = response.getOutputStream();
            //输出图片的工具
            //三个参数：1.要输出的图片 2.格式 3.使用的流
            ImageIO.write(image,"png",os);
            //response是由SpringMVC管理的，不需要自己手动关闭os流
        } catch (IOException e) {
            //出现异常，输出日志
            logger.error("响应验证码失败:"+e.getMessage());
        }

    }

}
