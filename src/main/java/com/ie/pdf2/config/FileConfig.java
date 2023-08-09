package com.ie.pdf2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.io.File;

@Configuration
public class FileConfig {

    @Value("${filename}")
    private String filename;

    // # Redis服务器连接端口
    @Value("${temp}")
    private String temp;

    // # Redis服务器连接密码（默认为空）
    @Value("${pdf}")
    private String pdf ;

    @Bean
    public void CreateFile() {

        String path = filename + "/" +temp;
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        path = filename + "/" +pdf;
        folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        System.out.println("ok");
    }


}
