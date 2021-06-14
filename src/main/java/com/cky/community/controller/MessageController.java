package com.cky.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.cky.community.common.Response;
import com.cky.community.model.Message;
import com.cky.community.model.MessageAc;
import com.cky.community.model.User;
import com.cky.community.service.MessageService;
import com.cky.community.service.UserService;
import com.cky.community.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RestController
public class MessageController {
    @Autowired
    MessageService messageService;
    @Autowired
    UserService userService ;

    @RequestMapping("/getConversations")
    public Response getConversations(HttpServletRequest request, int page, int size ){
        String userId = CookieUtils.getValue(request, "userId");
        HashMap conversations = messageService.getConversations(Integer.parseInt(userId), page, size);
        return Response.success(conversations);
    }
    @RequestMapping("/getLetterDetails")
    public  Response getLetterDetails(String conversationId){
        HashMap<String, Object> letterDetails = messageService.getLetterDetails(conversationId);
        return  Response.success(letterDetails);

    }
    @RequestMapping("/addMessage")
    public Response addMessage(Message message){
        messageService.addMessage(message);
        return Response.success("发送成功");

    }
    @RequestMapping("/sendMessage")
    public Response sendMessage(HttpServletRequest request,String toUserName,String content){
        String userId = CookieUtils.getValue(request, "userId");
        Message message = new Message();
        message.setFromId(Integer.parseInt(userId));
        User user = userService.getUserByName(toUserName);
        message.setToId(user.getId());
        message.setContent(content);
        String conversation_id = message.getFromId()+"_"+message.getToId();
        message.setConversationId(conversation_id);
        messageService.addMessage(message);
        return Response.success("发送成功");
    }
    @RequestMapping("/readMsg")
    public Response readMsg(String conversationId,int userId){
        messageService.readMsg(conversationId,userId);
        return Response.success("已读");
    }

    @RequestMapping("/getNotices")
    public Response getNotices(int userId){
        HashMap<String, Object> notices = messageService.getNotices(userId);
        return Response.success(notices,"查询成功");
    }
    @RequestMapping("/getNoticeDetail")
    public Response getNoticeDetail(String conversationId,int userId,int page,int size){
        List<Message> noticeDetails = messageService.getNoticeDetail(conversationId, userId,page,size);
        System.out.println(noticeDetails.size());
        ArrayList<HashMap<String,Object>> list = new ArrayList();
        for(Message message:noticeDetails){
            HashMap<String,Object> map = new HashMap();
            HashMap content = JSONObject.parseObject(message.getContent(), HashMap.class);
            Integer entityType =(Integer) content.get("entityType");
            Integer fromUserId = (Integer) content.get("userId");
            map.put("fromUserId",fromUserId);
            map.put("fromUsername",userService.getUserById(fromUserId).getUsername());
            map.put("entityType",entityType);
            map.put("entityId",content.get("entityId"));
            map.put("username",userService.getUserById(message.getToId()).getUsername());
            map.put("time",message.getCreateTime());
            list.add(map);
        }


        return Response.success(list,"查询成功");
    }
    @RequestMapping("/getNoticeDetailNums")
    public Response getNoticeDetailNums(int userId,String conversationId){
        int noticeDetailNums = messageService.getNoticeDetailNums(userId, conversationId);
        return Response.success(noticeDetailNums);

    }
}

