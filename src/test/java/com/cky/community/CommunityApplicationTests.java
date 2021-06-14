package com.cky.community;

import com.alibaba.fastjson.JSONObject;
import com.cky.community.dao.DiscussPostDao;
import com.cky.community.dao.MessageDao;
import com.cky.community.dto.DiscussPostInfo;
import com.cky.community.model.CommentAC;
import com.cky.community.model.DiscussPost;
import com.cky.community.model.MessageAc;
import com.cky.community.service.DiscussPostService;
import com.cky.community.service.SearchService;
import com.cky.community.service.UserService;
import com.cky.community.utils.EmailSender;
import com.cky.community.utils.SensitiveWordUtil;
import com.cky.community.vo.CommentVo;
import com.cky.community.vo.DiscussPostVo;
import org.apache.ibatis.annotations.Update;
import org.apache.lucene.util.QueryBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;


//让测试类的自动注入等注解生效
@RunWith(SpringRunner.class)
@SpringBootTest
class CommunityApplicationTests {
    @Autowired
    MessageDao messageDao;
    @Autowired
    EmailSender emailSender;
    @Autowired
    SensitiveWordUtil sensitiveWordUtil;
    @Autowired
    DiscussPostService discussPostService;
    @Resource
    DiscussPostDao discussPostDao;
    @Autowired
    RestHighLevelClient restHighLevelClient;
    @Autowired
    UserService userService;
    @Autowired
    SearchService searchService;
    @Test
    public void emailtest(){
        emailSender.sendMail("2574049767@qq.com","激活账号","<div><p><b>"+"2574049767@qq.com"+"</b>, 您好!</p><p>\n" +
                "您正在注册牛客网, 这是一封激活邮件, 请点击 <a href>此链接</a>,激活您的牛客账号!</p></div>");

    }
    @Test
    public void sessitiveTest(){
        String s="我爱嫖娼吸毒和赌博";
        String s1 = sensitiveWordUtil.filterWord(s);
        System.out.println(s1);

    }
    @Test
    public void test1(){
        List<MessageAc> conversations = messageDao.getConversations(111, 0, 5);
        for(MessageAc messageAc:conversations){
            System.out.println(messageAc);
        }
    }
    @Test
    public void CreateIndexTest() throws IOException {
        //创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("discuss_post");
        //执行请求
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse);

    }
    @Test
    void testExistIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("test");
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }
    @Test
    void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("test");
        AcknowledgedResponse delete = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());
    }
    @Test
    void testAddDocument() throws IOException {

        //创建请求
        IndexRequest request = new IndexRequest("test");
        //put
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(10));
        String s = JSONObject.toJSONString(userService.getUserById(111));
        request.source(s, XContentType.JSON);
        IndexResponse index = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        System.out.println(index.toString());

    }
    @Test
    public void  testIsExists(){
        GetIndexRequest request = new GetIndexRequest("test","1");

    }
    @Test
    public void testGetDoc() throws IOException {
        GetRequest request = new GetRequest("test","1");
        GetResponse getResponse = restHighLevelClient.get(request, RequestOptions.DEFAULT);
        System.out.println(getResponse.getSourceAsString());

    }
    @Test
    public void testUpdateDoc() throws IOException {
        UpdateRequest request = new UpdateRequest("test","1");
//        request.doc()
        UpdateResponse update = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        System.out.println(update);

    }
    @Test
    void testBulkRequest() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");
        List<DiscussPost> allDiscussPost = discussPostService.getAllDiscussPost();
        for(DiscussPost discussPost:allDiscussPost){
            bulkRequest.add(new IndexRequest("discuss_post")
            .source(JSONObject.toJSONString(discussPost),XContentType.JSON));
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulkResponse.hasFailures());
    }
    //searchRequest
    //searchSourceBuilder
    //HighlighterBuilder
    //TermQueryBuilder`
    @Test
    void testSearch() throws IOException {
        SearchRequest searchRequest = new SearchRequest("discuss_post");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        searchSourceBuilder.query(matchAllQueryBuilder);
//        searchSourceBuilder.highlighter();
//        searchSourceBuilder.from();
        searchSourceBuilder.size(20);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(JSONObject.toJSONString(searchResponse.getHits()));
        System.out.println("===================");
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap());
        }


    }
    @Test
    void test()  {
        DiscussPostInfo discussPostInfo = new DiscussPostInfo();
        discussPostInfo.setOffset(0);
        discussPostInfo.setSize(10);
        discussPostInfo.setUserId(0);
        discussPostInfo.setMode(0);
        List<DiscussPostVo> discussPosts = discussPostService.getDiscussPosts(discussPostInfo);
        for (DiscussPostVo discussPost : discussPosts) {
            System.out.println(discussPost);
        }

    }

    }




