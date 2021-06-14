package com.cky.community.config;

import lombok.Data;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * @author cky
 * @create 2021-05-08 14:49
 */
@Configuration
@MapperScan(basePackages = "com.cky.community.dao",sqlSessionTemplateRef = "communitySqlSessionTemplate")
public class CommunityDataSourceConfig {


    @Autowired
    CommunityDataConfig dataConfig;

    @Bean("communityDataSource")
    public DataSource communityDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(dataConfig.driverClassName)
                .url(dataConfig.getUrl())
                .username(dataConfig.username)
                .password(dataConfig.password)
                .build();
    }
    @Bean
    @ConfigurationProperties(prefix = "mybatis.configuration")
    public org.apache.ibatis.session.Configuration globalConfiguration(){
        return new org.apache.ibatis.session.Configuration();
    }

    @Bean("communitySqlSessionFactory")
    public SqlSessionFactory communityFactory(@Qualifier("communityDataSource") DataSource dataSource,org.apache.ibatis.session.Configuration config) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setConfiguration(config);
        return bean.getObject();
    }

    @Bean(name = "communityTransactionManager")
    public DataSourceTransactionManager testTransactionManager(@Qualifier("communityDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "communitySqlSessionTemplate")
    public SqlSessionTemplate testSqlSessionTemplate(
            @Qualifier("communitySqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
