package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginTicketService {
    @Autowired
    private LoginTicketMapper loginTicketMapper;

    //so we now in service layer ,what should we do ?
    //call the dao layer , to finish service develop
    //we want to implement the login function
    //NOTICE:answer is
    //实现业务层-->is used by controller layer
    //包括对用户名/密码的判断，验证是否已经存在该user，
    //如果存在，需要返回一个唯一的ticket。

    //return map type can record many kinds of messages
    public Map<String,Object> login(String userId){
        //don't know what to do ,let's init the map
        Map<String,Object> map = new HashMap<>();




        return map;
    }



}
