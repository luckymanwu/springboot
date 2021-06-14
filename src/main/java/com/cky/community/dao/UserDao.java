package com.cky.community.dao;

import com.cky.community.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author cky
 * @create 2021-05-08 18:02
 */
@Mapper
public interface UserDao {

    @Select("select * from user where id=#{id} ")
    User getUserById(int id);
    @Select("select * from user where username=#{username} ")
    User selectByName(String username);
    @Select("select * from user where email=#{email} ")
    User selectByEmail(String email);
    @Insert("insert into user(username, password, salt, email, type, status, activation_code, header_url, create_time)" +
            "values(#{username}, #{password}, #{salt}, #{email}, #{type}, #{status}, #{activation_code}, #{header_url}, #{create_time})")
    int insertUser(User user);
    @Update("update user set status=#{status} where id =#{id} ")
    int updateStatus(int id, int status);
    @Update("update user set header_url=#{headerUrl} where id =#{id} ")
    int updateHeader(int id, String headerUrl);
    @Update("update user set password=#{password} where id =#{id} ")
    int updatePassword(int id, String password);


}
