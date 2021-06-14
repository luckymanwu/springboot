package com.cky.community.interceptors;

import com.cky.community.service.StatisticsServer;
import com.cky.community.service.UserService;
import com.cky.community.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Component
public class StatisticsInterceptor implements HandlerInterceptor {
    @Autowired
    StatisticsServer statisticsServer;


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 统计UV
        String ip = request.getRemoteHost();
        statisticsServer.recordUV(ip);

        String userId = CookieUtils.getValue(request, "userId");
        if (userId != null) {
            statisticsServer.recordDAU(Integer.valueOf(userId));
        }
        return true;
    }
}
