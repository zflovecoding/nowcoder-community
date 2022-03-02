package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

//ticket is login credentials(登陆凭证)
@Mapper
@Repository
public interface LoginTicketMapper {
    //insert ticket
    //here we use annotation to impl the method in Mybatis
    //instead of xx-mapper.xml
    //the format is like "@Insert({"","",""})"
    @Insert({"insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"})
    //set the id auto increment
    //配置文件中的mybatis.configuration.useGeneratedKeys=true
    //对注解形式的SQL是不生效的
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);
    //select by ticket
    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);
    //for developing logout function , we need to a method to change the status
    @Update({"update login_ticket set status=#{status} where ticket=#{ticket}"})
    int updateStatus(String ticket, int status);
    //here , we have finished this function's data access layer develop
    //now, we should go to service layer
    //nope,we should test if we have some error
}
