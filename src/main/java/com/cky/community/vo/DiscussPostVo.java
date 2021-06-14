package com.cky.community.vo;

import com.cky.community.model.DiscussPost;
import com.cky.community.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author cky
 * @create 2021-05-08 18:20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscussPostVo {
    private int id;
    private String headerUrl;
    private String content;
    private String username;
    private int status;
    private int type;
    private int userId;
    private Date createTime;
    private String title;
    private int like;
    private int replyNum;

}

