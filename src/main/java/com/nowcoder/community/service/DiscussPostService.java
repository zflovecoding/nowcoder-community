package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    //service layer calls dao layer
    //now the method has not different functions compared with doa layer
    //but maybe we will add function in future added in service layer
    public List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit){
        return discussPostMapper.selectDiscussPosts(userId,offset,limit);
    }

    public int selectDiscussPostRows(int userID){
        return discussPostMapper.selectDiscussPostRows(userID);
    }
}
