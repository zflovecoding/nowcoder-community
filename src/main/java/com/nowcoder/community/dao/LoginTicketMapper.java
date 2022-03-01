package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;
//ticket is login credentials(登陆凭证)
@Mapper
public interface LoginTicketMapper {
    //insert ticket
    //here we use annotation to impl the method in Mybatis
    //instead of xx-mapper.xml
    //the format is like "@Insert({"","",""})"
    @Insert({"insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"})
    //set the id auto increment
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);
    //select by ticket
    @Select({"select * from login_ticket where ticket = #{ticket}"})
    LoginTicket selectByTicket(String ticket);
    //for developing logout function , we need to a method to change the status
    @Update({"update login_ticket set status=#{status} where ticket=#{ticket}"})
    int updateStatus(String ticket, int status);
    
}
