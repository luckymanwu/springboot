package com.cky.community.controller;

import com.cky.community.common.CommunityConstant;
import com.cky.community.common.Response;
import com.cky.community.service.LikeService;
import com.cky.community.utils.CookieUtils;
import com.cky.community.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@RestController
public class LikeController implements CommunityConstant {
    @Autowired
    LikeService likeService;
    @Autowired
    RedisTemplate redisTemplate;
    @RequestMapping("/like")
    public Response like(HttpServletRequest request,int entityType, int entityId, int entityUserId){
        int userId = Integer.valueOf(CookieUtils.getValue(request, "userId"));
        HashMap like = likeService.like(entityType, entityId, entityUserId,userId);
        String scoreKey = RedisUtils.getScoreKey();
        if(entityType==ENTITY_TYPE_POST){
            redisTemplate.opsForSet().add(scoreKey,entityId);
        }
        return Response.success(like);
    }

}
