package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;
//用于持有用户信息，代替session对象，而且是线程隔离带
@Component
public class HostHolder {
    private ThreadLocal<User> users =new ThreadLocal<>();
    //ThreadLocal有两个主要方法，set存值，get取值
    //根据线程为key存值
    public void setUser(User user){
        users.set(user);
    }
    public User getUser(){
        return users.get();
    }
    public void clear(){
        users.remove();
    }


}
