package com.nowcoder.community.dao;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/*
unfinished functions:
1. 根据ID查询一个用户。
2. 根据用户名查询一个用户。
3. 根据邮箱查询一个用户。
4. 插入一个用户。
5. 根据ID修改用户的状态。
6. 根据ID修改用户的头像路径。
7. 根据ID修改用户的密码。
* */
//just define the interface ,no need to write implements classes
@Repository
@Mapper
public interface UserMapper {
    //1.select user by ID
    User selectById(int id);
    //2.select user by UserName
    User selectByName(String username);
    //3.select user by email
    User selectByEmail(String email);
    //4.insert a user
    int insertUser(User user);
    //5.update user status by userId
    int updateStatus(int id,int status);
    //6.update avatar path by userId
    int updateHeader(int id,String headerUrl);
    //7.update user password by userID
    int updatePassword(int id,String password);
}
