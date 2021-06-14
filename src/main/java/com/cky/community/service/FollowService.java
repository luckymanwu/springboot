package com.cky.community.service;

import com.cky.community.Event.EventProducer;
import com.cky.community.common.CommunityConstant;
import com.cky.community.dao.UserDao;
import com.cky.community.model.Event;
import com.cky.community.model.User;
import com.cky.community.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    UserDao userDao;
    @Autowired
    EventProducer eventProducer;
    private static final Logger LOGGER = LoggerFactory.getLogger(FollowService.class);

    public void  subscribe(int userId,int targetId){
        String prefixPersonFollowKey = RedisUtils.getPrefixPersonFollowKey(userId);
        String prefixPersonFanKey = RedisUtils.getPrefixPersonFanKey(targetId);
        redisTemplate.opsForZSet().add(prefixPersonFollowKey,targetId,System.currentTimeMillis());
        redisTemplate.opsForZSet().add(prefixPersonFanKey,userId,System.currentTimeMillis());
        Event event = new Event();
        event.setTopic(TOPIC_FOLLOW);
        event.setEntityType(ENTITY_TYPE_USER);
        event.setEntityId(targetId);
        event.setUserId(userId);
        event.setEntityUserId(targetId);
        eventProducer.fireEvent(event);
    }
    public void unSubscribe(int userId,int targetId ){
        String prefixPersonFollowKey = RedisUtils.getPrefixPersonFollowKey(userId);
        String prefixPersonFanKey = RedisUtils.getPrefixPersonFanKey(targetId);
        redisTemplate.opsForZSet().remove(prefixPersonFollowKey,targetId);
        redisTemplate.opsForZSet().remove(prefixPersonFanKey,userId);
        Event event = new Event();
        event.setTopic(TOPIC_UNSUBSCRIBE);
        event.setEntityType(ENTITY_TYPE_USER);
        event.setEntityId(targetId);
        event.setUserId(userId);
        event.setEntityUserId(targetId);
        eventProducer.fireEvent(event);
    }

    public boolean hasFollowed(int userId,int targetId){
        String prefixPersonFollowKey = RedisUtils.getPrefixPersonFollowKey(userId);
        return redisTemplate.opsForZSet().score(prefixPersonFollowKey, targetId) !=null;
    }
    public long  getFansNum(int userId){
        String prefixPersonFanKey = RedisUtils.getPrefixPersonFanKey(userId);
        Long size = redisTemplate.opsForZSet().zCard(prefixPersonFanKey);
        return size;

    }
    public long getFollowerNum(int userId){
        String prefixPersonFollowKey = RedisUtils.getPrefixPersonFollowKey(userId);
        Long size = redisTemplate.opsForZSet().zCard(prefixPersonFollowKey);
        return size;
    }
    public List<HashMap<String,Object>>  getFans(int page,int size,int userId,int targetId){
        ArrayList list = new ArrayList();
        int offset = (page-1)*size;
        String prefixPersonFanKey = RedisUtils.getPrefixPersonFanKey(targetId);
        Set<Integer> FansIds = redisTemplate.opsForZSet().reverseRange(prefixPersonFanKey, offset, offset + size - 1);
        if(FansIds==null){
            return  null;
        }
        for(Integer fansId:FansIds){
            HashMap<String,Object> map = new HashMap();
            boolean hasFollowed = hasFollowed(userId, fansId);
            map.put("hasFollowed",hasFollowed);
            User fans = userDao.getUserById(fansId);
            map.put("fans",fans);
            Double score = redisTemplate.opsForZSet().score(prefixPersonFanKey, fansId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);

        }
        return  list;
    }
    public List<HashMap<String,Object>>  getFollowers(int page,int size,int userId,int targetId){
        ArrayList list = new ArrayList();
        int offset = (page-1)*size;
        String prefixPersonFollowKey = RedisUtils.getPrefixPersonFollowKey(targetId);
        Set<Integer> FollowerIds = redisTemplate.opsForZSet().reverseRange(prefixPersonFollowKey, offset, offset + size - 1);
        if(FollowerIds==null){
            return  null;
        }
        for(Integer followerId:FollowerIds){
            HashMap<String,Object> map = new HashMap();
            User follower = userDao.getUserById(followerId);
            boolean hasFollowed = hasFollowed(userId, followerId);
            map.put("hasFollowed",hasFollowed);
            map.put("Follower",follower);
            Double score = redisTemplate.opsForZSet().score(prefixPersonFollowKey, followerId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }


}
