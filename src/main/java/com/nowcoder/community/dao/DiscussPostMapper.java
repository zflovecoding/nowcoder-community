package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface DiscussPostMapper {
    //method
    //userId param is for future feature:my posts,normally select situation
    //userId is 0 ,so here is a dynamic sql statement
    //offset and limit is for the pagination(分页) function
    //offset is starting line number of each page
    //limit is the number each page shows
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);
    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.
    //@Param annotation is used to alias the parameter
    //if there is only one param,and it will be used in <if>,then it must be added a @Param annotation
    int selectDiscussPostRows(@Param("userId") int userID);


}
