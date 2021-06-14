package com.cky.community.Event;
import com.alibaba.fastjson.JSONObject;
import com.cky.community.common.CommunityConstant;
import com.cky.community.model.DiscussPost;
import com.cky.community.model.Event;
import com.cky.community.model.Message;
import com.cky.community.service.DiscussPostService;
import com.cky.community.service.MessageService;
import com.cky.community.service.SearchService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;

@Component
public class EventConsumer implements CommunityConstant {
    @Autowired
    MessageService messageService;
    @Autowired
    DiscussPostService discussPostService;
    @Autowired
    SearchService searchService;

    @KafkaListener(topics={TOPIC_LIKE,TOPIC_FOLLOW,TOPIC_COMMENT,TOPIC_UNSUBSCRIBE})
    public void handleMessage(ConsumerRecord record){
        if(record==null || record.value()==null){
            System.out.println("消息的内容为空");
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            System.out.println("消息格式有误");
        }
        Message message = new Message();
        message.setConversationId(event.getTopic());
        message.setFromId(SYSTEM);
        message.setToId(event.getEntityUserId());
        message.setCreateTime(new Date());
        message.setStatus(UNREAD);
        HashMap<String,Object> content = new HashMap();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());
        content.put("extraData",event.getData());
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);

    }
    @KafkaListener(topics={TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if(record==null || record.value()==null){
            System.out.println("消息内容不能为空");
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            System.out.println("消息格式有误");
        }
        DiscussPost post = discussPostService.getDiscussPostById(event.getEntityId());
        searchService.saveDiscussPost(post);

    }
    @KafkaListener(topics={TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
        if(record==null || record.value()==null){
            System.out.println("消息内容不能为空");
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            System.out.println("消息格式有误");
        }
        DiscussPost post = discussPostService.getDiscussPostById(event.getEntityId());
        searchService.saveDiscussPost(post);

    }


}
