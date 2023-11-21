package cn.zhuhai.usercenter.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @Author Ewng
 * @Date 2023/11/21 9:37
 */
@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test01() {
        // 字符串
        ValueOperations ops = redisTemplate.opsForValue();
        ops.set("god", 666);
        System.out.println(ops.get("god"));
        ops.set("zk", 000);

    }
}
