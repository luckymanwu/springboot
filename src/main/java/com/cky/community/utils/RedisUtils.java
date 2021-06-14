package com.cky.community.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisUtils {
    @Autowired
    RedisTemplate redisTemplate;
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_PERSON_LIKE = "like:person";
    private static final String PREFIX_PERSON_FAN ="fan:person";
    private static final String PREFIX_PERSON_FOLLOWER ="follower:person";
    private static final String PREFIX_USER="user";
    private static final String PREFIX_UV="uv";
    private static final String PREFIX_DAU="dau";
    private static final String PREFIX_POST="post";
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }
    public static String getPersonLikeKey(int entityUserId){
        return  PREFIX_PERSON_LIKE + SPLIT+entityUserId;
    }
    public static String getPrefixPersonFanKey(int userId){
        return PREFIX_PERSON_FAN + SPLIT+userId;
    }
    public static String getPrefixPersonFollowKey(int userId){
        return PREFIX_PERSON_FOLLOWER + SPLIT+userId;
    }

    public static String getPrefixUser(int userId){
        return PREFIX_USER+SPLIT+userId;
    }

    //单日uv
    public static String getPrefixUvKey(String date){
        return PREFIX_UV+SPLIT+date;
    }
    //区间uv
    public static String getPrefixUvKey(String startDate,String endDate){
        return PREFIX_UV+SPLIT+startDate+SPLIT+endDate;
    }
    //单日dau
    public static String getPrefixDauKey(String date){
        return PREFIX_DAU+SPLIT+date;
    }
    //区间dau
    public static String getPrefixDauKey(String startDate,String endDate){
        return PREFIX_DAU+SPLIT+startDate+SPLIT+endDate;
    }
    public static String getScoreKey(){
        return PREFIX_POST+SPLIT+"score";
    }
}
