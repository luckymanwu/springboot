package com.cky.community.dao;

import com.cky.community.dto.DiscussPostInfo;
import com.cky.community.model.Comment;
import com.cky.community.model.CommentAC;
import com.cky.community.model.DiscussPost;
import com.cky.community.service.DiscussPostService;
import com.cky.community.vo.DiscussPostDetailVo;
import com.cky.community.vo.DiscussPostVo;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author cky
 * @create 2021-05-08 13:49
 */
@Mapper
public interface DiscussPostDao {
    @Select("<script>select d.*,u.header_url,u.username,u.create_time from discuss_post d" +
            " inner join  user u  " +
            "on d.user_id = u.id " +
            "where d.status!=2 <if test='userId != 0'> and u.userId=#{userId}</if> " +
            "<if test='mode == 0'> order by d.type desc,d.create_time desc </if> " +
            "<if test='mode == 1'> order by d.type desc,d.score desc </if>" +
            "limit #{offset},#{size} </script> ")
    List<DiscussPostVo> getDiscussPosts(DiscussPostInfo discussPostInfo);

    @Select("<script> select count(id) from discuss_post" +
            " where status!=2 <if test='userId != 0'> and userId=#{userId}</if> </script> ")
    int getDiscussPostsRows(int userId);

    @Insert("insert into discuss_post(user_id,title,content,type,status,create_time,comment_count,score)" +
            " values (#{userId},#{title},#{content},#{type},#{status},#{createTime},#{commentCount},#{score}) ")
    void addDiscussPost(DiscussPost post);
    @Select("select d.user_id,d.*,u.header_url,u.username,u.create_time from discuss_post d inner join  user u  " +
            "on d.user_id = u.id where d.id =#{postId}  ")
    DiscussPostDetailVo getDiscussPostDetail(int postId);
    @Select("select u.username,r.id , r.entity_type,r.entity_id,r.content,r.status,r.target_id,r.create_time ,r.user_id from (select c2.* from comment c2 right join \n" +
            "(select * from comment  where entity_id=#{postId} and entity_type =1) AS c1 \n" +
            "on c1.id = c2.entity_id where c2.entity_type =2) AS r inner join user u on r.user_id=u.id" +
            " order by r.create_time ")
    List<CommentAC> getCTC(int postId );
   @Select("select c.id,c.user_id,c.entity_type,c.entity_id,c.target_id,c.content,c.status,c.create_time,u.username " +
           " from comment c inner join user u on c.user_id = u.id" +
           " where c.entity_id=#{postId} and c.status=0 and c.entity_type=1" +
           " order by create_time limit #{offset},#{size} ")
    List<CommentAC> getCTD(int postId, int offset, int size );
   @Select("select count(id) from comment where entity_id=#{postId} and entity_type=1 ")
   int getCommentNum(int postId);
   @Insert("insert into comment(user_id,entity_type,entity_id,target_id,content,status,create_time) " +
           "values(#{user_id},#{entity_type},#{entity_id},#{target_id},#{content},#{status},#{create_time}) ")
   void addComment(Comment comment);
   @Select("select * from discuss_post ")
   List<DiscussPost> getAllDiscussPost();
   @Update("update discuss_post set status = #{status} where id= #{postId} ")
   void updatePostStatus(int postId,int status);
    @Update("update discuss_post set type = #{type} where id= #{postId} ")
    void updatePostType(int postId,int type);
    @Select("select * from discuss_post where id=#{postId}")
    DiscussPost selectDiscussPostById(int postId);
    @Update("update discuss_post set score=#{score} where id=#{postId} ")
    void updateScore(int postId,double score);

}
