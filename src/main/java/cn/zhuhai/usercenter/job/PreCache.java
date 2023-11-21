package cn.zhuhai.usercenter.job;

import cn.zhuhai.usercenter.mapper.UserMapper;
import cn.zhuhai.usercenter.model.domain.User;
import cn.zhuhai.usercenter.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.zhuhai.usercenter.constant.UserConstant.REDIS_KEY_PREFIX;

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

    @Resource
    private RedissonClient redissonClient;

    // 重点用户
    private List<Long> mainUserList = Arrays.asList(1L);

    @Scheduled(cron = "0 0 0 * * *")
    public void doCacheRecommendUser() {
        // 设置分布式锁
        String lockName = String.format("%s:precachejob:docache:lock", REDIS_KEY_PREFIX);
        RLock lock = redissonClient.getLock(lockName);
        // 尝试获取锁
        try {
            // 只有一个程序可以获取锁
            if (lock.tryLock(0, 30000L, TimeUnit.MILLISECONDS)) {
                log.info("lock info : get lock");
                // 遍历重点用户
                for (Long userId :mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format("%s:user:recommend:%s", REDIS_KEY_PREFIX, userId);
                    ValueOperations<String, Object> ops = redisTemplate.opsForValue();
                    // 写缓存
                    try {
                        ops.set(redisKey, userPage, 1, TimeUnit.DAYS);
                    }catch (Exception e) {
                        log.error("redis set key error: "+e.getMessage());
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("redis exception"+e);
        } finally {
            // 释放锁
            // 判断当前锁是否是当前线程加的锁 -> 解决：谁加锁谁释放， 防止自己的锁被别人释放掉
            if (lock.isHeldByCurrentThread()) {
                log.info("lock info : unlock");
                lock.unlock();
            }

        }


    }
}
