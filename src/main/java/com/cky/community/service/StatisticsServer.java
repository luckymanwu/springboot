package com.cky.community.service;

import com.cky.community.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

@Service
public class StatisticsServer  {
    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    @Autowired
    private RedisTemplate redisTemplate;
    public void recordUV(String ip){
        String prefixUvKey = RedisUtils.getPrefixUvKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(prefixUvKey, ip);
    }
    // 将指定用户计入DAU
    public void recordDAU(int userId) {
        String redisKey = RedisUtils.getPrefixDauKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    public long getUV(Date start,Date end){
        ArrayList<String> keyLists = new ArrayList<>();
        if(start==null||end==null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String UvKey = RedisUtils.getPrefixUvKey(df.format(calendar.getTime()));
            keyLists.add(UvKey);
            calendar.add(Calendar.DATE, 1);
        }
        String redisKey = RedisUtils.getPrefixUvKey(df.format(start), df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey, keyLists.toArray());
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }
    public long getDAU(Date start,Date end){
        ArrayList<byte[]> keyLists = new ArrayList<>();
        if(start==null||end==null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String DAUKey = RedisUtils.getPrefixDauKey(df.format(calendar.getTime()));
            keyLists.add(DAUKey.getBytes());
            calendar.add(Calendar.DATE, 1);
        }
        // 进行OR运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey = RedisUtils.getPrefixDauKey(df.format(start), df.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), keyLists.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });

    }

}
