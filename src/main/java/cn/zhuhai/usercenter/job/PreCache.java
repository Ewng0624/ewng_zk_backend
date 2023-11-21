package cn.zhuhai.usercenter.job;

import cn.zhuhai.usercenter.mapper.UserMapper;
import cn.zhuhai.usercenter.model.domain.User;
import cn.zhuhai.usercenter.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author Ewng
 * @description 缓存预热
 * @Date 2023/11/21 11:31
 */
@Slf4j
@Component
public class PreCache {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    // 重点用户
    private List<Long> mainUserList = Arrays.asList(1L);


    @Scheduled(cron = "0 0 0 * * *")
    public void doCacheRecommendUser() {
        for (Long userId :mainUserList) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
            String redisKey = String.format("zk:user:recommend:%s", userId);
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            // 写缓存
            try {
                ops.set(redisKey, userPage, 1, TimeUnit.DAYS);
            }catch (Exception e) {
                log.error("redis set key error");
            }
        }
    }
}
