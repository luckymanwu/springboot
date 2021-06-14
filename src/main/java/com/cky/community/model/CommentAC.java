package com.cky.community.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentAC {
   private int id;
   private int userId;
   private String username;
   private int entityType;
   private int entityId;
   private int targetId;
   private String targetName;
   private String content;
   private int status;
   private Date createTime;
   private int likes;

}
