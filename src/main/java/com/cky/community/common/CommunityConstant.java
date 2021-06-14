package com.cky.community.common;

public interface CommunityConstant {
    String TOPIC_LIKE="like";
    String TOPIC_FOLLOW ="follow";
    String TOPIC_COMMENT="comment";
    String TOPIC_UNSUBSCRIBE="unSubscribe";
    String TOPIC_PUBLISH ="publish";
    String TOPIC_DELETE ="delete";
    int ENTITY_TYPE_POST = 1;
    int ENTITY_TYPE_COMMENT = 2;
    int ENTITY_TYPE_USER = 3;
    int SYSTEM = 1;
    int UNREAD=0;
    int READ=1;
    int DELETE=2;
    int USER=0;
    int ADMIN =1;
    int MODERATOR =2;
}
