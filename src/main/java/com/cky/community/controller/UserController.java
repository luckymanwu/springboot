package com.cky.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.cky.community.common.Response;
import com.cky.community.dto.LoginInfo;
import com.cky.community.dto.RegisterInfo;
import com.cky.community.model.LoginTicket;
import com.cky.community.model.User;
import com.cky.community.service.UserService;
import com.cky.community.utils.CommunityUtil;
import com.cky.community.utils.ValidateCode;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;




@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    Producer kpatch;
    @Value("${server.servlet.context-path}")
    String contextPath;

    @RequestMapping("/register")
    public Response register(RegisterInfo registerInfo) {
        Response response = userService.RegisterUser(registerInfo);
        return response;
    }

    @RequestMapping("/checkUserName")
    public Response checkUserName(String username) {
        Response response = userService.checkUserName(username);
        return response;

    }

    @RequestMapping("/checkUserEmail")
    public Response checkUserEmail(String email) {
        Response response = userService.checkUserEmail(email);
        return response;
    }

    @RequestMapping("/activate")
    public Response activate(String username, String code) {
        User user = userService.getUserByName(username);
        if (user.getStatus() == 1) {
            return Response.error("该用户已经激活");
        } else if (!user.getActivationCode().equals(code)) {
            return Response.error("激活码错误");
        } else {
            userService.updateStatus(user.getId(), 1);
            return Response.success("激活成功");
        }
    }

    @RequestMapping("/getAuthCode")
    public Response getAuthCode() {
        JSONObject resultObject = new JSONObject();
        ValidateCode validateCode = new ValidateCode();
        try {
            resultObject = validateCode.create();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        resultObject.put("uuid", uuid);
        String VerificationCode = String.valueOf(resultObject.get("VerificationCode"));
        return Response.success(resultObject);
    }

        @RequestMapping("/login")
        public Response login (LoginInfo loginInfo,HttpServletResponse response){
            String username = loginInfo.getUsername();
            String password = loginInfo.getPassword();
            User user = userService.getUserByName(username);
            Map<String, Object> map = userService.login(username, password);
            if(map.containsKey("token")){
                Cookie cookie1 =  new Cookie("userId", String.valueOf(user.getId()));
                cookie1.setPath("/");
                cookie1.setMaxAge(1000*60*24*30);
                Cookie cookie2 = new Cookie("token", map.get("token").toString());
                cookie2.setPath("/");
                cookie2.setMaxAge(1000*60*24*30);
                response.addCookie(cookie1);
                response.addCookie(cookie2);
                return Response.success(map);
            }else{
                return Response.error(map);
            }
    }

}
