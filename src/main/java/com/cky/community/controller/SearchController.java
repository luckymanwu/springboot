package com.cky.community.controller;

import com.cky.community.common.Response;
import com.cky.community.common.CommunityConstant;
import com.cky.community.model.User;
import com.cky.community.service.LikeService;
import com.cky.community.service.SearchService;
import com.cky.community.service.UserService;
import com.cky.community.vo.DiscussPostVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class SearchController implements CommunityConstant {
    @Autowired
    SearchService searchService;
    @Autowired
    UserService userService;
    @Autowired
    LikeService likeService;
    @RequestMapping("/search")
    public Response search(int page,int size,String key){
        try {
            List<DiscussPostVo> discussPosts = searchService.discussPostSearch(page, size, key);
            for (DiscussPostVo discussPost : discussPosts) {
                User user = userService.getUserById(discussPost.getUserId());
                discussPost.setHeaderUrl(user.getHeaderUrl());
                discussPost.setUsername(user.getUsername());
                int entityLikes = likeService.getEntityLikes(ENTITY_TYPE_POST, discussPost.getId());
                discussPost.setLike(entityLikes);
            }
            return Response.success(discussPosts);
        } catch (IOException e) {
            System.out.println("搜索出现异常");
        }
        return Response.error("");
    }
    @RequestMapping("/searchNum")
    public Response searchNum(String key){
        int total = 0;
        try {
            total = searchService.discussPostSearchNum(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.success(total);
    }

}
