package com.cky.community.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.cky.community.common.Response;
import com.cky.community.model.Profile;
import com.cky.community.model.User;
import com.cky.community.service.FollowService;
import com.cky.community.service.UserService;
import com.cky.community.utils.CommunityUtil;
import com.cky.community.utils.CookieUtils;
import com.cky.community.utils.JWTUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SettingController {
    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${community.path.domain}")
    private String domain;
    @Autowired
    UserService userService;
    @Autowired
    FollowService followService;
    @RequestMapping("/changePassword")
    public Response changePassword(int userId, String password, String newPassword){
        Map<String, Object> result = userService.changePassword(userId, password, newPassword);
        if(result.containsKey("pwdError")){
            return Response.error(result.get("pwdError"));
        }
        return  Response.success("修改密码成工");
    }
    @CrossOrigin
    @RequestMapping("/upload")
    public Response upload(MultipartFile avatar ,HttpServletRequest request)  {
        String token = request.getHeader("Authorization");
        DecodedJWT verify = JWTUtils.verify(token);
        String username = verify.getClaim("username").asString();
        User user = userService.getUserByName(username);
        String fileName = avatar.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        fileName =username + suffix;
        File dest = new File(uploadPath  + fileName);
        try {
            // 存储文件
            avatar.transferTo(dest);
        } catch (IOException e) {
//            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }
        String headUrl = domain +"/community/header/"+fileName;
        userService.updateHeader(user.getId(), headUrl);
        return Response.success(headUrl);
    }

    @RequestMapping("/header/{fileName}")
    public void getHead(@PathVariable("fileName") String fileName,HttpServletResponse response){
        // 服务器存放路径
        fileName = uploadPath + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
//            logger.error("读取头像失败: " + e.getMessage());
        }

    }
    @RequestMapping("/getMenu")
    public Response getMenu(HttpServletRequest request){
        boolean isLogin=false;
        HashMap map = new HashMap();
        String token = CookieUtils.getValue(request, "token");
        if(!StringUtils.isBlank(token)){
            DecodedJWT verify = JWTUtils.verify(token);
            String username = verify.getClaim("username").asString();
            User user = userService.getUserByName(username);
            isLogin =true;
            map.put("isLogin",isLogin);
            map.put("user",user);
            return Response.success(map);
        }else{
            map.put("isLogin",isLogin);
            return Response.error(map);
        }

    }
    @RequestMapping("/getProfile")
    public Response getProfile(int userId,int targetId) {
        HashMap map = userService.getProfile(userId, targetId);
        return Response.success(map);
    }
    @RequestMapping("/getFans")
    public Response getFans(int page,int size,int userId,int targetId){
        List<HashMap<String, Object>> fans = followService.getFans( page, size,userId,targetId);
        return Response.success(fans);
    }
    @RequestMapping("/getFollowers")
    public Response getFollowers(int page,int size,int userId,int targetId){
        List<HashMap<String, Object>> followers = followService.getFollowers( page, size,userId,targetId);
        return Response.success(followers);
    }
    @RequestMapping("/getFollowersNum")
    public Response getFollowersNum(int userId){
        long followerNum = followService.getFollowerNum(userId);
        return Response.success(followerNum);

    }
    @RequestMapping("/getFansNum")
    public Response getFansNum(int userId){
        long fansNum = followService.getFansNum(userId);
        return Response.success(fansNum);
    }
    @RequestMapping("/subscribe")
    public Response subscribe(int userId,int targetId){
        followService.subscribe(userId,targetId);
        return Response.success("关注成功");
    }
    @RequestMapping("/unsubscribe")
    public Response unsubscribe(int userId,int targetId){
        followService.unSubscribe(userId,targetId);
        return Response.success("取关成功");
    }

}
