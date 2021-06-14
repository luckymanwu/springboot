package com.cky.community.service;

import com.alibaba.fastjson.JSONObject;
import com.cky.community.dao.MessageDao;
import com.cky.community.dao.UserDao;
import com.cky.community.model.Message;
import com.cky.community.model.MessageAc;
import com.cky.community.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class MessageService {

    @Resource
    MessageDao messageDao;
    @Resource
    UserDao userDao;
    @Autowired
    UserService userService;
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);
    public HashMap getConversations(int userId,int page,int size ){
        int offset = (page-1)*size;
        HashMap map = new HashMap();
        List<MessageAc> conversations = messageDao.getConversations(userId, offset, size);
        for(MessageAc messageAc:conversations){
            if(userId==messageAc.getFromId()){
                User user1 = userDao.getUserById(messageAc.getToId());
                messageAc.setHeadUrl(user1.getHeaderUrl());
                messageAc.setFromUsername(user1.getUsername());
                messageAc.setTargetId(messageAc.getToId());
            }else{
                User user =  userDao.getUserById(messageAc.getFromId());
                messageAc.setHeadUrl(user.getHeaderUrl());
                messageAc.setFromUsername(user.getUsername());
                messageAc.setTargetId(messageAc.getFromId());
            }
            int letterCount = messageDao.selectLetterCount(messageAc.getConversationId());
            int unreadNum = messageDao.selectUnread(messageAc.getConversationId());
            messageAc.setCount(letterCount);
            messageAc.setUnread(unreadNum);
        }
        int unreadTotal = messageDao.selectTotalUnread(userId);
        int conversationCount = messageDao.getConversationCount(userId);
        map.put("unreadTotal",unreadTotal);
        map.put("conversations",conversations);
        map.put("conversationCount",conversationCount);
        return map;

    }
    
    public HashMap<String,Object> getLetterDetails(String  conversationId ){
        HashMap map = new HashMap();
        List<Message> letters = messageDao.selectLetters(conversationId);
        if(letters!=null){
            User user = userDao.getUserById(letters.get(0).getFromId());
            map.put("fromUsername",user.getUsername());
            map.put("headUrl",user.getHeaderUrl());
        }
        int letterNum = messageDao.selectLetterCount(conversationId);
        map.put("letterNum",letterNum);
        map.put("letters",letters);
        return map;

    }
    public void addMessage(Message message){
       message.setCreateTime(new Date());
       message.setStatus(0);
       messageDao.addMessage(message);

    }
    public void readMsg(String conversationId,int userId ){ messageDao.readMsg(conversationId,userId); }
    public HashMap<String,Object> getNotices(int userId){
        HashMap map = new HashMap();
        int unreadTotal=0;
        List<MessageAc> Notices = messageDao.selectNotice(userId);
        for(MessageAc messageAc:Notices){
            messageAc.setCount(messageDao.selectNoticeNum(userId,messageAc.getConversationId()));
            int unreadNoticeNum = messageDao.selectUnreadNoticeNum(userId, messageAc.getConversationId());
            unreadTotal+=unreadNoticeNum;

            messageAc.setUnread(unreadNoticeNum);
            HashMap content = JSONObject.parseObject(messageAc.getContent(), HashMap.class);
            Integer id = (Integer)content.get("userId");
            messageAc.setFromUsername(userService.getUserById(id).getUsername());
            messageAc.setEntityType((Integer)content.get("entityType"));
            map.put(messageAc.getConversationId(),messageAc);
        }
        map.put("unreadTotal",unreadTotal);
        return map;
    }
   public List<Message> getNoticeDetail(String conversationId,int userId,int page,int size){
        int offset = (page-1)*size;
       List<Message> noticeDetails = messageDao.getNoticeDetail(conversationId, userId,offset,size);
       return noticeDetails;

   }
   public int getNoticeDetailNums(int userId,String conversationId){
       return messageDao.selectNoticeNum(userId,conversationId);

   }

}
