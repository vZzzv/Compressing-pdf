package com.ie.pdf2.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration //该类为配置类
public class RestTemplateConfig {

    @Bean
    @LoadBalanced //添加这个后，可以自动识别 nacos 并做负载均衡
    public RestTemplate restTemplate(){
        return  new RestTemplate();
    }
}
