package com.cky.community.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author cky
 * @create 2021-05-08 13:37
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussPost implements Serializable {
    private int id;
    private int userId;
    private String title;
    private String content;
    private int type;
    private int status;
    private Date createTime;
    private int commentCount;
    private double score;


}
