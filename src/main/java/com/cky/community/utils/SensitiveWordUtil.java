package com.cky.community.utils;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;

@Component
public class SensitiveWordUtil {
    // 根节点
    private TreeNode rootNode = new TreeNode();

    public  String filterWord(String content){
        if (StringUtils.isBlank(content)) {
            return null;
        }
        // 指针1
        TreeNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();
        while(begin<content.length()&&position<content.length()) {
            char key = content.charAt(position);
            if (isSymbol(key)) {
                // 若指针1处于根节点,将此符号计入结果,让指针2向下走一步
                if (tempNode == rootNode) {
                    sb.append(key);
                    begin++;
                    position=begin;
                }
                // 无论符号在开头或中间,指针3都向下走一步
                position++;
                continue;
            }
            tempNode = tempNode.getSubNode(key);
            if(tempNode==null){
                sb.append(content.charAt(begin));
                begin++;
                position=begin;
                tempNode=rootNode;
            }else if(tempNode.isSensitive()){
                sb.append("***");
                begin=++position;
                tempNode=rootNode;
            }else{
                position++;
            }

        }
        sb.append(content.substring(begin));
        return sb.toString();
    }

    @PostConstruct
    public  void init(){
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("SensitiveWord.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while((line=reader.readLine())!=null){
                TreeNode tempNode = rootNode;
                for(int i=0; i<line.length();i++){
                    char c = line.charAt(i);
                    TreeNode subNode = tempNode.getSubNode(c);
                    if(subNode==null){
                        tempNode.addSubNode(c,new TreeNode());
                    }
                    tempNode= tempNode.getSubNode(c);;
                    if(i==line.length()-1){
                        tempNode.setSensitive(true);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    private class TreeNode{
        private boolean isSensitive=false;
        private HashMap<Character,TreeNode> subNode = new HashMap<>();;
        private boolean isSensitive() {
            return isSensitive;
        }
        private void setSensitive(boolean sensitive) {
            isSensitive = sensitive;
        }

        public TreeNode getSubNode(Character c) {
            return subNode.get(c);
        }
        private void  addSubNode(Character c, TreeNode node){
            subNode.put(c,node);

        }
    }
}
