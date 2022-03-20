package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.*;

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

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    //due to DiscussPost , we should get user by userId
    public User getUserByID(int userID) {
        return userMapper.selectById(userID);
    }
    public User findUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }
    //写一个注册方法:返回的内容是多种信息，账号不能重复，不能为空，等等等的很多信息，所以要返回集合做好封装
    //帮助我们把传来的用户数据存到数据库里面
    //具体包括：用户查重，加密密码，发送用户激活码
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        //空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;//直接返回消息
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }
        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            //账号存在
            map.put("usernameMsg", "该账号已存在.");
            return map;
        }

        //邮箱验证
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            //账号存在
            map.put("emailMsg", "该邮箱已被注册.");
            return map;
        }
        //此时用户没有问题，开始注册
        //设置密码，还需要salt用于拼接密码后加密
        //需要对user中的数据进行补充，补充需要存到数据库里的，但是不是user输入的

        //不需要那么长的salt，5位数就行
        user.setSalt(CommunityUtil.generateUUid().substring(0, 5));
        //有了salt对密码进行设置
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
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
        Context context = new Context();
        //传递值给模板
        context.setVariable("email", user.getEmail());
        //传递的URL格式：http ://localhost:15213/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        //第一个参数传入处理的模板是什么
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活邮箱", content);
        return map;
        //此时Service层已经完成，去开发控制器Controller
    }

    //activate by email method
    //http://localhost:15213/community/activation/101/code
    public int activate(int userId, String code) {
        User user = userMapper.selectById(userId);
        //如果已经是正式用户，那就是重复激活
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            //激活没问题,status=1是正式用户
            //不能只user.setStatus,这是要更新数据库的操作，需要使用DA层，也就是使用UserMapper
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            //activateCode is incorrect
            return ACTIVATION_FAILED;
        }
    }

    //so we now in service layer ,what should we do ?
    //call the dao layer , to finish service develop
    //we want to implement the login function
    //NOTICE:answer is,and login is a user's action,so we write it here in userService
    //实现业务层-->is used by controller layer
    //包括对用户名/密码的判断，验证是否已经存在该user，
    //如果存在，需要返回一个唯一的ticket。


    /**
     * @param username       just username
     * @param password       these params were from the view layer
     * @param expiredSeconds hope to pass how many seconds the credentials will expire,here change it's type from int to long
     *                       because uses int it will over flow
     * @return return map type can record many kinds of messages
     */
    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        //don't know what to do ,let's init the map
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该用户不存在，请重新注册");
            return map;
        }
        //验证密码
        //犯错记录：数据库中存的密码是加密后的，不能直接拿明文比较,而且有salt
        if (!CommunityUtil.md5(password + user.getSalt()).equals(user.getPassword())) {
            map.put("passwordMsg", "密码错误，请重新输入");
            return map;
        }
        //第一次忘记了：验证账号状态
        //status 0-未激活，1-已激活
        if (user.getStatus() == 0) {
            //尚未激活的账号不可以登录
            map.put("usernameMsg", "该账号未激活，请激活后再登录");
            return map;
        }
        //验证也没有问题，可以登录了
        //设置一下ticket的其他值
        //生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setTicket(CommunityUtil.generateUUid());
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);//这里的status不是user表的，是login_ticket的，0-有效，1-无效
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));//设置过期时间
        loginTicketMapper.insertLoginTicket(loginTicket);//生成凭证并插入表中
        //凭证要发给客户端，所以最后返回的结果里，要把凭证放进map里
        map.put("ticket", loginTicket.getTicket());
        return map;
        //接下来就是表现层的故事了，接收到三个值，传回来，交给UserService处理
    }
    //使登陆凭证失效
    public void logout(String ticket) {
        //是不是这个1代表的失效也放到常量接口里最好
        loginTicketMapper.updateStatus(ticket,1);
    }



    //第一个自己添加的功能：忘记密码

//    public Map<String,Object> forgetPwd(String email,String verifyCode,String newPassword){
//        Map<String,Object> map = new HashMap<>();
//        //校验邮箱，不可以是未注册邮箱
//        User user = userMapper.selectByEmail(email);
//        if(user==null){
//            map.put("emailMsg","该邮箱未被注册,请注册");
//            return map;
//        }
//        //如果是已经注册的邮箱，那么发一封邮件给他，包含验证码，这个验证码需要保存下来校验使用
//        //构建一封邮件，我认为包含验证码的text文件即可，但是HTML设置样式更现代
//        //发送邮件的方式：先初始化context，设置HTML模板的值
//        Context context = new Context();
//        //传递值给模板setVariable
//        context.setVariable("email",email);
//        String generatedVerifyCode = CommunityUtil.generateUUid().substring(0,6);
//        //session.setAttribute("verifyCode",generatedVerifyCode);
//        context.setVariable("verifyCode",generatedVerifyCode);
//        //通过模板引擎，将值拼接到模板中，并且生成html内容，这里使用的模板有问题
//        String content = templateEngine.process("/mail/forget", context);
//        //之后调用写好的sendMail()发送文件
//        mailClient.sendMail(email,"修改密码",content);
//        //之后是检验验证码,这个验证码是生成了发到邮箱里的,但是本地应该也要保存的
          //这个逻辑不正确，应该是前面表单给回来的数据，和本地保存在session的验证码比较，这个方法不对
//        if(!generatedVerifyCode.equals(verifyCode)){
//            map.put("codeMsg","您输入的验证码不正确");
//            return map;
//        }
//        //此时说明验证码也正确，可以修改密码变成新密码
//        //想起来一个问题，明文传送的密码到数据库里要加密保存
//        //拿到用户的salt值用来拼接密码，以免下次用户无法登录
//        String salt = user.getSalt();
//        userMapper.updatePassword(user.getId(),CommunityUtil.md5(newPassword+salt));
//        return map;
//    }


    //忘记密码，发邮件给客户，这是点击获取验证码的时候触发的功能，需要做的是核验邮箱和想办法保存验证码到服务器
    public String forgetPwd(String email){
//        HashMap<String, Object> map = new HashMap<>();
//        User user = userMapper.selectByEmail(email);
//        //如果这个用户的邮箱未注册
//        if(user==null){
//            map.put("userMsg","该邮箱尚未注册");
//            return  map;
//        }
        //验证邮箱之后，生成验证码，保存，并发送邮件
        Context context = new Context();
        context.setVariable("email",email);
        //生成验证码
        String code =  CommunityUtil.generateUUid().substring(0,6);
        context.setVariable("verifyCode",code);
        //注意模板
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email,"忘记密码",content);
        return code;
    }
    //重置密码
    //这是一个涉及到修改数据库内容的操作，一定是在service层操作的
    //重置密码提交的表单内容包含：验证码（校验），注册的邮箱（找到user，修改密码）
    public  Map<String,Object> resetPwd(String email,String newPwd){
        //这里要对传入的email和password做校验
        Map<String, Object> map = new HashMap<>();
        if(StringUtils.isBlank(email)){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        if(StringUtils.isBlank(newPwd)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        User user = userMapper.selectByEmail(email);
        if(user==null){
            //注意这里是emailMsg
            map.put("emailMsg","该邮箱尚未注册");
            return map;
        }

        userMapper.updatePassword(user.getId(),CommunityUtil.md5(newPwd+user.getSalt()));
        //此处已经重置了，map.put("user",user)?是为什么呢--->在controller层，可以做检查，
        // 如果map包含了user这个key，证明校验没问题，且reset成功，可以重定向去登陆了
        map.put("user",user);
        return map;
    }

    public LoginTicket findLoginTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }

}
