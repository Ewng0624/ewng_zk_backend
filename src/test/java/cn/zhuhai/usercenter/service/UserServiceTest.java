package cn.zhuhai.usercenter.service;
import java.util.Date;


import cn.zhuhai.usercenter.mapper.UserMapper;
import cn.zhuhai.usercenter.model.domain.User;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;


/*
    用户测试
 */
@SpringBootTest
public class UserServiceTest {
    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    @Test
    void userRegister() {
        String userAccount = "zhaoliu";
        String password = "12345678";
        String checkPassword = "12345678";
        long id = userService.userRegister(userAccount, password, checkPassword);
        System.out.println(id);
    }

    @Test
    void searchUsersByTags() {
        List<String> tagNameList = Arrays.asList("java", "python");
        List<User> userList = userService.searchUsersByTags(tagNameList);
        Assert.assertNotNull(userList);
    }

    @Test
    void generateUser() {
        User user = new User();

        user.setUsername("张三");
        user.setUserAccount("zhangsan");
        user.setHeadUrl("");
        user.setGender(1);
        // 数据库为明文 //
        user.setUserPassword("12345678");
        user.setPhone("");
        user.setEmail("");
        user.setUserStatus(0);
        user.setUserRole(0);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDelete(0);
        user.setTags("Java, 男, 大二");
        boolean save = userService.save(user);
        Assert.assertTrue(save);
    }

    @Test
    void updateUser() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", 3);
        User user = userMapper.selectOne(queryWrapper);
        boolean result = user == null? false : true;
        Assert.assertTrue(result);
    }

}