package com.cky.community.service;

import com.cky.community.Event.EventProducer;
import com.cky.community.common.Response;
import com.cky.community.common.CommunityConstant;
import com.cky.community.dao.DiscussPostDao;
import com.cky.community.dao.UserDao;
import com.cky.community.dto.DiscussPostInfo;
import com.cky.community.model.*;
import com.cky.community.utils.RedisUtils;
import com.cky.community.utils.SensitiveWordUtil;
import com.cky.community.vo.CommentVo;
import com.cky.community.vo.DiscussPostDetailVo;
import com.cky.community.vo.DiscussPostVo;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author cky
 * @create 2021-05-08 14:39
 *
 */
@Service
public class DiscussPostService implements CommunityConstant {
    @Resource
    UserDao userDao;
    @Resource
    DiscussPostDao discussPostDao;
    @Autowired
    SensitiveWordUtil sensitiveWordUtil;
    @Autowired
    LikeService likeService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    EventProducer eventProducer;
    @Autowired
    SearchService searchService;
    // 牛客纪元
    private static final Date epoch;
    // 帖子列表缓存
    private LoadingCache<String, List<DiscussPostVo>> postListCache;

    // 帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;
    @Value("${caffeine.maximumSize}")
    private int maxSize;
    @Value("${caffeine.expireSeconds}")
    private int expireSeconds;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败!", e);
        }
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscussPostService.class);
    @PostConstruct
    public void init(){
        postListCache =Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPostVo>>() {
                    @Override
                    public @Nullable List<DiscussPostVo> load(@NonNull String key) throws Exception {
                        if(key==null|| key.length() == 0){
                            throw new IllegalArgumentException("参数错误!");
                        }
                        String[] params = key.split(":");
                        if(params==null||params.length!=2){
                            throw new IllegalArgumentException("参数错误!");
                        }
                        String offset = params[0];
                        String size = params[1];
                        DiscussPostInfo discussPostInfo = new DiscussPostInfo();
                        discussPostInfo.setOffset(Integer.valueOf(offset));
                        discussPostInfo.setSize(Integer.valueOf(size));
                        discussPostInfo.setUserId(0);
                        discussPostInfo.setMode(1);
                      return   discussPostDao.getDiscussPosts(discussPostInfo);

                    }
                });
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(@NonNull Integer key) throws Exception {
                        return  discussPostDao.getDiscussPostsRows(key) ;
                    }
                });

    }
    public List<DiscussPostVo> getDiscussPosts(DiscussPostInfo discussPostInfo){
        System.out.println(discussPostInfo.getMode());
        List<DiscussPostVo> discussPosts;
        if(discussPostInfo.getMode()==1){
            String key = discussPostInfo.getOffset()+":"+discussPostInfo.getSize();
             discussPosts = postListCache.get(key);
        }else{
            discussPosts = discussPostDao.getDiscussPosts(discussPostInfo);
        }
        for(DiscussPostVo dis: discussPosts){
            int entityLikes = likeService.getEntityLikes(0, Integer.valueOf(dis.getId()));
            int total = discussPostDao.getCommentNum(Integer.valueOf(dis.getId()));
            dis.setReplyNum(total);
            dis.setLike(entityLikes);
        }
        return discussPosts;
    }
    public  int getDiscussPostsRows(int userId){
        if (userId==0){
            return  postRowsCache.get(userId);
        }
        return  discussPostDao.getDiscussPostsRows(userId);
    }

    public Response addDiscussPost(DiscussPost post){
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        post.setTitle(sensitiveWordUtil.filterWord(post.getTitle()));
        post.setContent(sensitiveWordUtil.filterWord(post.getContent()));
        discussPostDao.addDiscussPost(post);
        String redisKey = RedisUtils.getScoreKey();
//        redisTemplate.opsForSet().add(redisKey, post.getId());
       return Response.success("发帖成功");
      }
    public DiscussPostDetailVo getDiscussPostDetail(int postId){
        DiscussPostDetailVo discussPostDetailVo = discussPostDao.getDiscussPostDetail(postId);
        int entityLikes = likeService.getEntityLikes(0, postId);
        discussPostDetailVo.setLike(entityLikes);
        return discussPostDetailVo;
    }
    public CommentVo getComments(int postId, int page, int size){
        List<CommentAC> ctd = discussPostDao.getCTD(postId, (page - 1) * size, size);
        List<CommentAC> ctc = discussPostDao.getCTC(postId);
        int total = discussPostDao.getCommentNum(postId);
        for(CommentAC c1 :ctd){
            if(c1.getTargetId()!=0){
                String username = userDao.getUserById(c1.getTargetId()).getUsername();
                c1.setTargetName(username);
            }
            int entityLikes = likeService.getEntityLikes(1, c1.getId());
            c1.setLikes(entityLikes);
        }
        for(CommentAC c2 :ctc){
            if(c2.getTargetId()!=0){
                String username = userDao.getUserById(c2.getTargetId()).getUsername();
                c2.setTargetName(username);
            }
            int entityLikes = likeService.getEntityLikes(1, c2.getId());
            c2.setLikes(entityLikes);
        }
        CommentVo commentVo = new CommentVo();
        commentVo.setCtc(ctc);
        commentVo.setCtd(ctd);
        commentVo.setTotal(total);
        return  commentVo;
    }
    @Transactional
    public  int addComment(Comment comment){
        discussPostDao.addComment(comment);
        int commentNum = discussPostDao.getCommentNum(comment.getId());
        Event event = new Event();
        event.setTopic(TOPIC_COMMENT);
        event.setUserId(comment.getUserId());
        event.setEntityId(comment.getEntityId());
        event.setEntityUserId(comment.getTargetId());
        event.setEntityType(comment.getEntityType());
        eventProducer.fireEvent(event);
        return commentNum;
    }
    public List<DiscussPost> getAllDiscussPost(){
        List<DiscussPost> allDiscussPost = discussPostDao.getAllDiscussPost();
        return allDiscussPost;
    }
    public DiscussPost getDiscussPostById(int postId){
        return  discussPostDao.selectDiscussPostById(postId);
    }
    public void top(int postId,int type){
        discussPostDao.updatePostType(postId,type);
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(postId);
        eventProducer.fireEvent(event);
    }
    public void wonderful(int postId,int status){
        discussPostDao.updatePostStatus(postId,status);
        Event wonderfulEvent = new Event();
        wonderfulEvent.setEntityId(postId);
        wonderfulEvent.setTopic(TOPIC_PUBLISH);
        wonderfulEvent.setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(wonderfulEvent);
    }
    public void delete(int postId,int status){
        discussPostDao.updatePostStatus(postId,status);
        Event event = new Event();
        event.setTopic(TOPIC_DELETE);
        event.setEntityId(postId);
        event.setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);
    }
    public void updateScore(int postId,double score){
        discussPostDao.updateScore(postId,score);
    }
    public void score(int postId){
        DiscussPost post = getDiscussPostById(postId);

        // 是否精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.getEntityLikes(ENTITY_TYPE_POST, postId);
        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 更新帖子分数
        updateScore(postId, score);
        // 同步搜索数据
        post.setScore(score);
        searchService.saveDiscussPost(post);

    }
   @Scheduled(cron="0 0/15 * * * ?")
    public void  refreshScore(){
        String scoreKey = RedisUtils.getScoreKey();
        Set<String> postIds = redisTemplate.opsForSet().members(scoreKey);
        for (String postId : postIds) {
            score(Integer.valueOf(postId));
        }
    }
}
