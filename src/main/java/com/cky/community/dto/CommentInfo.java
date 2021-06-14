package com.cky.community.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentInfo {
    private int userId;
    private int entityType;
    private String comment;
    private int entityId;
    private int targetId;
}
