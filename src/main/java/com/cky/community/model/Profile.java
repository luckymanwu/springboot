package com.cky.community.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    private String username;
    private String headUrl;
    private Date createTime;
    private long followers;
    private long fans;
    private int likes;
}
