package com.cky.community.vo;

import com.cky.community.model.CommentAC;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentVo {
   private List<CommentAC> ctd;
   private List<CommentAC> ctc;
   private int total;

}
