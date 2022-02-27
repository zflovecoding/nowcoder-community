package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;


    //the funtion which is when user clicked "注册" in home page,we should direct to /register page
    @RequestMapping(path="/register",method = RequestMethod.GET)
    public String accessRegisterPage(){
        return "/site/register";
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

}
