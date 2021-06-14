package com.cky.community.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscussPostInfo {
    private int offset;
    private int userId;
    private int page;
    private int size;
    private int mode;
}
