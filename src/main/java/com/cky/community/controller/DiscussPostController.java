package com.cky.community.controller;

import com.cky.community.common.Response;
import com.cky.community.common.CommunityConstant;
import com.cky.community.model.Comment;
import com.cky.community.model.DiscussPost;
import com.cky.community.model.User;
import com.cky.community.service.DiscussPostService;
import com.cky.community.service.UserService;
import com.cky.community.utils.CommunityUtil;
import com.cky.community.utils.CookieUtils;
import com.cky.community.utils.RedisUtils;
import com.cky.community.vo.CommentVo;
import com.cky.community.vo.DiscussPostDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

@RestController
public class DiscussPostController implements CommunityConstant {
    @Autowired
    DiscussPostService discussPostService;
    @Autowired
    UserService userService;
    @Autowired
    RedisTemplate redisTemplate;
    String scoreKey = RedisUtils.getScoreKey();

    @RequestMapping("/publishDiscussPost")
    public Response publishDiscussPost(HttpServletRequest request,String title, String content){
        String token = CookieUtils.getValue(request, "token");
        int userId = CommunityUtil.getUserId(token);
        DiscussPost discussPost = new DiscussPost();
        discussPost.setContent(content);
        discussPost.setTitle(title);
        discussPost.setUserId(userId);
        discussPost.setCreateTime(new Date());
        Response response = discussPostService.addDiscussPost(discussPost);

        return  response;
    }
    @RequestMapping("/getDiscussPost")
    public Response getDiscussPost(int PostId){
        DiscussPostDetailVo discussPostDetail = discussPostService.getDiscussPostDetail(PostId);
        if(discussPostDetail==null){
            return  Response.error(discussPostDetail);
        }
        Response response = Response.success(discussPostDetail);
        return response;
    }
    @RequestMapping("/getDiscussPostComment")
    public Response<CommentVo> getDiscussPostComment(int postId , int page, int size){
        CommentVo comments = discussPostService.getComments(postId, page, size);
        return Response.success(comments);

    }
    @RequestMapping("/addComment")
    public Response addComment(HttpServletRequest request,Comment comment){
        String token = CookieUtils.getValue(request, "token");
        int userId = CommunityUtil.getUserId(token);
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        comment.setUserId(userId);
        int num = discussPostService.addComment(comment);
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            redisTemplate.opsForSet().add(scoreKey,comment.getEntityId());
        }
        return Response.success(num,"评论成功");

    }
    @RequestMapping("/top")
    public Response top(int postId, int type, int userId, HttpServletResponse response){
        User user = userService.getUserById(userId);
        if(user.getType()!=MODERATOR){
            return Response.error("权限不足");
        }
        discussPostService.top(postId,type);

        redisTemplate.opsForSet().add(scoreKey,postId);
        return Response.success("操作成功");

    }
    @RequestMapping("/wonderful")
    public Response wonderful(int postId,int status,int userId){
        User user = userService.getUserById(userId);
        if(user.getType()!=MODERATOR){
            return Response.error("权限不足");
        }
        discussPostService.wonderful(postId,status);
        redisTemplate.opsForSet().add(scoreKey,postId);
        return Response.success("操作成功");
    }
    @RequestMapping("/delete")
    public Response deleteDiscussPost(int postId,int status,int userId){
        User user = userService.getUserById(userId);
        if(user.getType()!=MODERATOR){
            return Response.error("权限不足");
        }
        discussPostService.delete(postId,status);
        return Response.success("删除成功");
    }


}
