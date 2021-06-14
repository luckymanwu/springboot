package com.cky.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.cky.community.common.Response;
import com.cky.community.dto.DiscussPostInfo;
import com.cky.community.model.DiscussPost;
import com.cky.community.model.User;
import com.cky.community.service.DiscussPostService;
import com.cky.community.service.UserService;
import com.cky.community.vo.DiscussPostVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author cky
 * @create 2021-05-08 14:43
 */

@RestController
public class HomeController {
    @Autowired
    DiscussPostService discussPostService;
    @Autowired
    UserService userService;
    @RequestMapping("/getDiscussPostVo")
    public  Response getDiscussPosts( DiscussPostInfo discussPostInfo){
        int offset = (discussPostInfo.getPage()-1)* discussPostInfo.getSize();
        discussPostInfo.setOffset(offset);
        List<DiscussPostVo> discussPostVoList = discussPostService.getDiscussPosts(discussPostInfo);
        int count = discussPostService.getDiscussPostsRows(discussPostInfo.getUserId());
        JSONObject json = new JSONObject();
        json.put("discussPosts",discussPostVoList);
        json.put("count",count);
        return Response.success(json,"success");
    }

}
