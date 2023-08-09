package com.ie.pdf2.config.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {

    @Value("${spring.redis.host}")
    private String host;

    // # Redis服务器连接端口
    @Value("${spring.redis.port}")
    private int port = 0;

    // # Redis服务器连接密码（默认为空）
    @Value("${spring.redis.password}")
    private String password = "0";

    // 链接超时时间 单位 ms（毫秒）
    @Value("${spring.redis.timeout}")
    private int timeout = 0;

    // 连接池最大连接数（使用负值表示没有限制）
    @Value("${spring.redis.lettuce.pool.max-active}")
    private int redisPoolMaxActive = -1;

    // 连接池最大阻塞等待时间（使用负值表示没有限制）
    @Value("${spring.redis.lettuce.pool.max-wait}")
    private int redisPoolMaxWait = -1;

    // # 连接池中的最大空闲连接
    @Value("${spring.redis.lettuce.pool.max-idle}")
    private int redisPoolMaxIdle = 8;

    // 连接池中的最小空闲连接
    @Value("${spring.redis.lettuce.pool.min-idle}")
    private int redisPoolMinIdle = 0;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory).build();
        return redisCacheManager;
    }

    //
    // 第0个仓库 商户
    @Bean(name = "redis")
    public StringRedisTemplate redisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(createJedisConnectionFactory(0, host, port, password, timeout));

        return template;
    }


    @SuppressWarnings("deprecation")
    public JedisConnectionFactory createJedisConnectionFactory(
            int dbIndex, String host, int port, String password, int timeout) {

        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setDatabase(dbIndex);
        jedisConnectionFactory.setHostName(host);
        jedisConnectionFactory.setPort(port);
        jedisConnectionFactory.setPassword(password);
        jedisConnectionFactory.setTimeout(timeout);
        jedisConnectionFactory.setPoolConfig(
                setPoolConfig(
                        redisPoolMaxIdle, redisPoolMinIdle, redisPoolMaxActive, redisPoolMaxWait, true));
        return jedisConnectionFactory;
    }

    private JedisPoolConfig setPoolConfig(
            int redisPoolMaxIdle2,
            int redisPoolMinIdle2,
            int redisPoolMaxActive2,
            int redisPoolMaxWait2,
            boolean b) {
        // TODO Auto-generated method stub
        return null;
    }
}
