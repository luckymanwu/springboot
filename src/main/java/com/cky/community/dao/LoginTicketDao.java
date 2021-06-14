package com.cky.community.dao;

import com.cky.community.model.LoginTicket;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LoginTicketDao {
    @Insert("insert into login_ticket(user_id,ticket,status,expired)values(#{userId},#{ticket},#{status},#{expired})")
    void insertTicket(LoginTicket loginTicket);
    @Update("update login_ticket set status=#{status} where user_id=#{userId} ")
    void updateTicketStatus(int userId,int status);
    @Select("select ticket from login_ticket where user_id=#{userId} ")
    String getTicket(int userId);

}
