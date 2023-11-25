package cn.zhuhai.usercenter.service;

import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Ewng
 * @Date 2023/11/21 16:26
 */
@SpringBootTest
public class RedissonTest {
    @Resource
    private RedissonClient redissonClient;

    @Test
    void test01() {
        // list
        List<String> list = new ArrayList<>();
        list.add("ewng");
        list.get(0);
//        list.remove(0);
        System.out.println("list-"+list.get(0));
        RList<String> rList = redissonClient.getList("list-redisson");
        rList.add("ewng");
        rList.get(0);
        System.out.println("rList-"+rList.get(0));
//        rList.remove(0);
        // map
        Map<String, Integer> map = new HashMap<>();
        map.put("ewng-map", 666);


        RMap<Object, Object> rmap = redissonClient.getMap("ewng-map");
        rmap.put("ewmg", 666);
        // set

        // stack

    }
}
