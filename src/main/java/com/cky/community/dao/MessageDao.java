package com.cky.community.dao;
import com.cky.community.model.Message;
import com.cky.community.model.MessageAc;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MessageDao {
    @Select("select * from message where id in (select max(id) from message " +
            "where status!=2 and from_id !=1 and (from_id =#{userId} or to_id =#{userId}) group by conversation_id)  " +
            "order by id desc limit #{offset},#{size} ")
    List<MessageAc> getConversations(int userId,int offset,int size);

    @Select("select  count(distinct conversation_id) from message " +
            "where status!=2 and from_id !=1 and (from_id =#{userId} or to_id =#{userId})" )
   int  getConversationCount(int userId);

    @Select("select * from message" +
            " where conversation_id =#{conversationId} and status!=2")
    List<Message> selectLetters(String conversationId);

    @Select("select count(id) from message where conversation_id=#{conversationId}")
    int selectLetterCount(String conversationId);

    @Select("select count(id) from message where conversation_id=#{conversationId} and status=0")
    int selectUnread(String conversationId);

    @Select("select count(id) from message where to_id =#{userId} and from_id !=1  and status=0")
    int selectTotalUnread(int userId );
    @Insert("insert into message(from_id,to_id,conversation_id,content,status,create_time) " +
            "values(#{from_id},#{to_id},#{conversation_id},#{content},#{status},#{create_time})")
    void addMessage(Message message);
    @Update("update message set status=1 where conversation_id=#{conversationId} and to_id =#{userId} ")
    void readMsg(String conversationId,int userId);
   @Select("select * from message where id in(select max(id) from message" +
           " where from_id=1 and to_id =#{userId} and status !=2 " +
           " group by conversation_id)")
    List<MessageAc> selectNotice(int userId);
   @Select("select count(id) from message" +
           " where conversation_id=#{conversationId} and from_id=1 and to_id =#{userId} and status=0  ")
    int selectUnreadNoticeNum(int userId,String conversationId );
    @Select("select count(id) from message" +
            " where conversation_id=#{conversationId} and from_id=1 and to_id =#{userId} and status !=2  ")
    int selectNoticeNum(int userId,String conversationId );
    @Select("select * from message where conversation_id=#{conversationId} and from_id=1 and to_id=#{userId} and status!=2  " +
            "order by create_time desc limit #{offset},#{size}")
     List<Message> getNoticeDetail(String conversationId,int userId,int offset,int size);


}

