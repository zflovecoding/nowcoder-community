package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    //注入邮件客户端，注册是用户的服务
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    //生成激活码要包含域名和项目名
    //所以都要注入进来
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;


    //due to DiscussPost , we should get user by userId
    public User getUserByID(int userID){
        return userMapper.selectById(userID);
    }

    //写一个注册方法:返回的内容是多种信息，账号不能重复，不能为空，等等等的很多信息，所以要返回集合做好封装
    //帮助我们把传来的用户数据存到数据库里面
    //具体包括：用户查重，加密密码，发送用户激活码
    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        //空值处理
        if(user==null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
            return map;//直接返回消息
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u!=null){
            //账号存在
            map.put("usernameMsg","该账号已存在.");
            return map;
        }
        
        //邮箱验证
        u = userMapper.selectByEmail(user.getEmail());
        if(u!=null){
            //账号存在
            map.put("emailMsg","该邮箱已被注册.");
            return map;
        }
        //此时用户没有问题，开始注册
        //设置密码，还需要salt用于拼接密码后加密
        //需要对user中的数据进行补充，补充需要存到数据库里的，但是不是user输入的

        //不需要那么长的salt，5位数就行
        user.setSalt(CommunityUtil.generateUUid().substring(0,5));
        //有了salt对密码进行设置
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        //设置其他字段
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        user.setType(0);//普通用户
        user.setStatus(0);//没有激活
        user.setActivationCode(CommunityUtil.generateUUid());
        //注册用户就是在数据库user表中添加一条user
        userMapper.insertUser(user);//insert之后我的user拥有了userId(自动生成，Mybatis配置过了，userMapper.xml)

        //注册，我们要给用户发激活邮件，一定是HTML格式，才可以携带激活链接
        //模板是：activation.html
        Context context  = new Context();
        //传递值给模板
        context.setVariable("email",user.getEmail());
        //传递的URL格式：http ://localhost:15213/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        //第一个参数传入处理的模板是什么
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(),"激活邮箱",content);
        return map;
        //此时Service层已经完成，去开发控制器Controller
    }

    //activate by email method
    //http://localhost:15213/community/activation/101/code
    public int activate(int userId,String code){
        User user = userMapper.selectById(userId);
        //如果已经是正式用户，那就是重复激活
        if(user.getStatus()==1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            //激活没问题,status=1是正式用户
            //不能只user.setStatus,这是要更新数据库的操作，需要使用DA层，也就是使用UserMapper
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }else {
            //activateCode is incorrect
            return ACTIVATION_FAILED;
        }
    }



}
