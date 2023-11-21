package cn.zhuhai.usercenter.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @Author Ewng
 * @Date 2023/11/21 16:14
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private String host;

    private String port;

    @Bean
    public RedissonClient redissonClient() {
        // 1. 创建配置
        Config config = new Config();
//        String redisAddress = "redis:192.168.160.130:6379";
        String redisAddress = String.format("redis://%s:%s",host, port);
        config.useSingleServer()
                .setAddress(redisAddress)
                .setDatabase(1)
                .setPassword("123456");
        // 2.创建Redis实例
        RedissonClient redisson = Redisson.create(config);

        return redisson;
    }
}
