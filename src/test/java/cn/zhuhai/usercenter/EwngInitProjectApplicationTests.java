package cn.zhuhai.usercenter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
    import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

@SpringBootTest
class EwngInitProjectApplicationTests {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void testDigest() {
        String newPassword = DigestUtils.md5DigestAsHex(("abcd" + "mypassword").getBytes());
        System.out.println(newPassword);
    }

    @Test
    void contextLoads() {
        ValueOperations<String, Object> ops = this.redisTemplate.opsForValue();
        ops.set("user_wjy", 666);
        Object value = ops.get("user_wjy");
        System.out.println(value);
    }

}
