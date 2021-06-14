package com.cky.community.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author cky
 * @create 2021-05-08 13:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private int id;
    private int fromId;
    private int toId;
    private String  conversationId;
    private   String content;
    private int status;
    private Date createTime;
}
