package com.cky.community.service;

import com.cky.community.dao.LoginTicketDao;
import com.cky.community.dto.LoginInfo;
import com.cky.community.model.LoginTicket;
import com.cky.community.model.Profile;
import com.cky.community.utils.CommunityUtil;
import com.cky.community.common.Response;
import com.cky.community.dao.UserDao;
import com.cky.community.dto.RegisterInfo;
import com.cky.community.model.User;
import com.cky.community.utils.EmailSender;
import com.cky.community.utils.JWTUtils;
import com.cky.community.utils.RedisUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.validation.Payload;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cky
 * @create 2021-05-08 18:22
 */
@Service
public class UserService {
    @Autowired
    UserDao userDao;
    @Autowired
    LoginTicketDao loginTicketDao;
    @Value("http://images.nowcoder.com/head/")
    String commonHeadUrl;
    @Autowired
    EmailSender emailSender;
    @Value("http://localhost:3005/#/OperateResult/")
    String FrontactivationUrl;
    @Value("http://localhost:8030/community/")
    String commonUrl;
    @Autowired
    LikeService likeService;
    @Autowired
    FollowService followService;
    @Autowired
    RedisTemplate redisTemplate;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public User getUserById(int id) {
        User userFromCache = getUserFromCache(id);
        if(userFromCache==null){
          return   initUser(id);
        }
        return userFromCache;
    }

    public User getUserByName(String username) {
        return userDao.selectByName(username);
    }

    public void updateStatus(int id, int status) {
        clearCache(id);
        userDao.updateStatus(id, status);
    }

    public Response checkUserName(String username) {
        User user = userDao.selectByName(username);
        System.out.println(user);
        if (user != null) {
            return Response.error("用户名已经存在");
        }
        return Response.success("用户名合法");
    }

    public Response checkUserEmail(String email) {
        if (userDao.selectByEmail(email) != null) {
            return Response.error("邮箱已注册");
        }
        return Response.success("邮箱合法");
    }


    public Response RegisterUser(RegisterInfo registerInfo) {
        String username = registerInfo.getUsername();
        String password = registerInfo.getPassword();
        String email = registerInfo.getEmail();
        if (StringUtils.isBlank(username)) {
            return Response.error("用户名不能为空");
        } else if (StringUtils.isBlank(password)) {
            return Response.error("密码不能为空");
        } else if (StringUtils.isBlank(email)) {
            return Response.error("邮箱不能为空");
        } else if (userDao.selectByName(username) != null) {
            return Response.error("用户名已经存在");
        } else if (userDao.selectByEmail(email) != null) {
            return Response.error("邮箱已注册");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        String salt = CommunityUtil.generateUUID().substring(0, 5);
        user.setSalt(salt);
        user.setPassword(CommunityUtil.md5(password + salt));
        int headRandomKey = (int) (Math.random() * 100);
        user.setHeaderUrl(commonHeadUrl + headRandomKey + "t.png");
        user.setType(0);
        user.setStatus(0);
        String activation_code = CommunityUtil.generateUUID();
        user.setActivationCode(activation_code);
        user.setCreateTime(new Date());
        String activationUrl = commonUrl + "activation/" + username + "/" + activation_code;
        userDao.insertUser(user);
        emailSender.sendMail(email, "激活账号", "<div><p><b>" + email + "</b>, 您好!</p><p>\n" +
                "您正在注册牛客网, 这是一封激活邮件<br/>" +
                " 您的激活码为:<b>" + activation_code + "<b/><br/>请点击 <a href=" + FrontactivationUrl + username + ">此链接</a>,激活您的牛客账号!</p></div>");
        return Response.success("");
    }

    public Map<String, Object> login(String username, String password) {
        HashMap map = new HashMap();
        HashMap payloads = new HashMap();
        User user = userDao.selectByName(username);
        if (user == null) {
            map.put("Error", "用户名或密码错误");
            return map;
        }
        if (!user.getPassword().equals(CommunityUtil.md5(password + user.getSalt()))) {
            map.put("Error", "用户名或密码错误");
            return map;
        } else if (user.getStatus() != 1) {
            map.put("Error", "用户名或密码错误");
            return map;
        }
        payloads.put("userId",String.valueOf(user.getId()));
        payloads.put("username", user.getUsername());
        String token = JWTUtils.getToken(payloads);
        map.put("token", token);
        map.put("userId",String.valueOf(user.getId()));
        return map;
    }

    public Map<String, Object> changePassword(int  userId, String password, String newPassword) {
        HashMap map = new HashMap();
        User user = getUserById(userId);
        if (!user.getPassword().equals(CommunityUtil.md5(password + user.getSalt()))) {
            map.put("pwdError", "密码错误");
            return map;
        }
        userDao.updatePassword(user.getId(), CommunityUtil.md5(newPassword + user.getSalt()));
        clearCache(userId);
           return map;

    }
    public int updateHeader(int userId, String header_url) {
        clearCache(userId);
        return userDao.updateHeader(userId, header_url);
    }

    public HashMap getProfile(int userId,int targetId){
        HashMap<String,Object> map = new HashMap();
        boolean isSubscribe = followService.hasFollowed(userId, targetId);
        User user = userDao.getUserById(targetId);
        int userLikes = likeService.getUserLikes(targetId);
        Profile profile = new Profile();
        profile.setCreateTime(user.getCreateTime());
        profile.setUsername(user.getUsername());
        profile.setHeadUrl(user.getHeaderUrl());
        profile.setLikes(userLikes);
        profile.setFans( followService.getFansNum(targetId));;
        profile.setFollowers(followService.getFollowerNum(targetId));
        map.put("isSubscribe",isSubscribe);
        map.put("profile",profile);
        return map;
    }
    public User  initUser(int userId){
        User user = userDao.getUserById(userId);
        String prefixUser = RedisUtils.getPrefixUser(userId);
        redisTemplate.opsForValue().set(prefixUser,user);
        return user;
    }
        public User getUserFromCache(int userId){
            String prefixUser = RedisUtils.getPrefixUser(userId);
            return (User)redisTemplate.opsForValue().get(prefixUser);
        }

        public void clearCache(int userId){
        String prefixUser = RedisUtils.getPrefixUser(userId);
        redisTemplate.delete(prefixUser);
        }
}
