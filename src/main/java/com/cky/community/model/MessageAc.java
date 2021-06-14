package com.cky.community.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageAc {
   private int id;
   private int fromId;
   private int toId;
   private String  conversationId;
   private   String content;
   private int status;
   private Date createTime;
   private int count;
   private int unread;
   private String FromUsername;
   private String headUrl;
   private int targetId;
   private int entityType;

}
