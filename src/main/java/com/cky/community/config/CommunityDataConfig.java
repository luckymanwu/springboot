package com.cky.community.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author cky
 * @create 2021-05-08 14:49
 */
@Data
@Component
@ConfigurationProperties(prefix = "community.datasource")
public class CommunityDataConfig {
    String url;
    String username;
    String password;
    String driverClassName;
}
