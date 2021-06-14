package com.cky.community.controller;

import com.cky.community.common.Response;
import com.cky.community.service.StatisticsServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class StatisticsController {
    @Autowired
    StatisticsServer statisticsServer;

    @RequestMapping("/getUV")
    public Response getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start, @DateTimeFormat(pattern = "yyyy-MM-dd")Date end){
        long uv = statisticsServer.getUV(start, end);
        return Response.success(uv);

    }
    @RequestMapping("/getDAU")
    public Response getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,@DateTimeFormat(pattern = "yyyy-MM-dd") Date end){
        long dau = statisticsServer.getDAU(start, end);
        return Response.success(dau);
    }
}
