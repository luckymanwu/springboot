package com.cky.community.service;

import com.cky.community.Event.EventProducer;
import com.cky.community.common.CommunityConstant;
import com.cky.community.model.Event;
import com.cky.community.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;


@Service
public class LikeService implements CommunityConstant {
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    EventProducer eventProducer;
    private static final Logger LOGGER = LoggerFactory.getLogger(LikeService.class);
    public HashMap like(int entityType, int entityId,int entityUserId,int userId) {
        HashMap map = new HashMap();
        String entityLikeKey = RedisUtils.getEntityLikeKey(entityType, entityId);
        String personLikeKey = RedisUtils.getPersonLikeKey(entityUserId);
        redisTemplate.opsForValue().increment(personLikeKey);
        redisTemplate.opsForValue().increment(entityLikeKey);
        map.put("entityLikes",redisTemplate.opsForValue().get(entityLikeKey));
        map.put("personLikes",redisTemplate.opsForValue().get(personLikeKey));
        Event event = new Event();
        event.setTopic(TOPIC_LIKE);
        event.setEntityType(entityType);
        event.setEntityUserId(entityUserId);
        event.setEntityId(entityId);
        event.setUserId(userId);
       eventProducer.fireEvent(event);
        return map;
    }

    public int getEntityLikes(int entityType, int entityId){
        String entityLikeKey = RedisUtils.getEntityLikeKey(entityType, entityId);
        Integer entityLikes = (Integer)redisTemplate.opsForValue().get(entityLikeKey);
        if(entityLikes==null){
            return 0;
        }
        return  entityLikes;
    }

    public int getUserLikes(int userId ){
        String personLikeKey = RedisUtils.getPersonLikeKey(userId);
        Integer UserLikes = (Integer) redisTemplate.opsForValue().get(personLikeKey);
        if(UserLikes==null){
            return 0;
        }
        return UserLikes;
    }



}
