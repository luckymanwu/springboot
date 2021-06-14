package com.cky.community.service;

import com.alibaba.fastjson.JSONObject;
import com.cky.community.model.DiscussPost;
import com.cky.community.vo.DiscussPostVo;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {
     @Autowired
     RestHighLevelClient restHighLevelClient;
     private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);
     public void saveDiscussPost(DiscussPost discussPost) {
          try {
               IndexRequest request = new IndexRequest("discuss_post");
               request.timeout("10s");
               String dis = JSONObject.toJSONString(discussPost);
               request.source(dis, XContentType.JSON);
               IndexResponse index = restHighLevelClient.index(request, RequestOptions.DEFAULT);
          } catch (IOException e) {
               e.printStackTrace();
          }

     }
     public  int discussPostSearchNum(String key) throws IOException {
          SearchRequest request = new SearchRequest("discuss_post");
          SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
          MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(key, "title", "content");
          searchSourceBuilder.query(multiMatchQueryBuilder);
          searchSourceBuilder.size(1000);
          request.source(searchSourceBuilder);
          SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
          return searchResponse.getHits().getHits().length;

     }
     public List<DiscussPostVo> discussPostSearch(int page, int size, String key) throws IOException {
          int from = (page-1)*size;
          SearchRequest searchRequest = new SearchRequest("discuss_post");
          //构建搜索条件
          SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
          MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(key, "title", "content");
          searchSourceBuilder.query(multiMatchQueryBuilder);
          searchSourceBuilder.sort(new FieldSortBuilder("type").order(SortOrder.DESC));
          searchSourceBuilder.sort(new FieldSortBuilder("score").order(SortOrder.DESC));
          searchSourceBuilder.sort(new FieldSortBuilder("create_time").order(SortOrder.DESC));

          //高亮
          HighlightBuilder highlightBuilder = new HighlightBuilder();
          HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field("title");
          highlightTitle.preTags("<em>").postTags("</em>");
          highlightBuilder.field(highlightTitle);
          HighlightBuilder.Field highlightContent = new  HighlightBuilder.Field("content");
          highlightContent.preTags("<em>").postTags("</em>");
          highlightBuilder.field(highlightContent);
          searchSourceBuilder.highlighter(highlightBuilder);

          searchSourceBuilder.from(from);
          searchSourceBuilder.size(size);
          searchRequest.source(searchSourceBuilder);

          SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
          SearchHit[] hits = searchResponse.getHits().getHits();
          List<DiscussPostVo> list = new ArrayList<>();
          for (SearchHit hit : hits) {
               Map<String,HighlightField> highlightFields = hit.getHighlightFields();
               DiscussPostVo post = new DiscussPostVo();

               String id = hit.getSourceAsMap().get("id").toString();
               post.setId(Integer.valueOf(id));

               String userId = hit.getSourceAsMap().get("user_id").toString();
               post.setUserId(Integer.valueOf(userId));


               HighlightField highTitle = highlightFields.get("title");
               if(highTitle!=null){
                    Text[] fragments = highTitle.fragments();
                    String newTitle ="";
                    for(Text text:fragments){
                         newTitle+=text;
                    }
                    hit.getSourceAsMap().put("title", HtmlUtils.htmlEscape(newTitle));

               }
               String title = hit.getSourceAsMap().get("title").toString();
               post.setTitle(title);

               HighlightField highContent = highlightFields.get("content");
               if(highContent!=null){
                    Text[] fragments = highContent.fragments();
                    String newContent ="";
                    for (Text text : fragments) {
                         newContent+=text;
                    }
                    hit.getSourceAsMap().put("content",HtmlUtils.htmlEscape(newContent));
               }
               String content = hit.getSourceAsMap().get("content").toString();
               post.setContent(content);

               String status = hit.getSourceAsMap().get("status").toString();
               post.setStatus(Integer.valueOf(status));

               String createTime = hit.getSourceAsMap().get("create_time").toString();
               post.setCreateTime(new Date(Long.valueOf(createTime)));

               String commentCount = hit.getSourceAsMap().get("comment_count").toString();
               post.setReplyNum(Integer.valueOf(commentCount));
               // 处理高亮显示的结果
               HighlightField titleField = hit.getHighlightFields().get("title");
               if (titleField != null) {
                    post.setTitle(titleField.getFragments()[0].toString());
               }

               HighlightField contentField = hit.getHighlightFields().get("content");
               if (contentField != null) {
                    post.setContent(contentField.getFragments()[0].toString());
               }

               list.add(post);
          }
          return list;
     }
}
