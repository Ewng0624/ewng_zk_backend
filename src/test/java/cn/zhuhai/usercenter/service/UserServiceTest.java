package cn.zhuhai.usercenter.service;


import cn.zhuhai.usercenter.model.domain.User;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.server.Session;

import javax.annotation.Resource;


/*
    用户测试
 */
@SpringBootTest
public class UserServiceTest {
    @Resource
    private UserService userService;

    @Test
    public void Test01() {
        User user = new User();

        user.setUsername("ewng");
        user.setUserAccount("123");
        user.setHeadUrl("D:\\desktop\\算法草图\\JVM&JRE&JDK关系图.png");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("456");
        boolean result = userService.save(user);

        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    @Test
    void userRegister() {
        String userAccount = "ewn";
        String password = "";
        String checkPassword = "12345";
        // 账户过短
//        long result = userService.userRegister(userAccount, password, checkPassword);
//        Assert.assertEquals(-1, result);
        // 密码过短
        // 账户
        userAccount = "ewngdog";
        password = "12345678";
        checkPassword = "12345678";
        //long result = userService.userRegister(userAccount, password, checkPassword);
        //Assert.assertEquals(1, result);
    }

//    @Test
//    void userLogin() {
//        String username = "ewngdog";
//        String password = "12345678";
//        User user = userService.userLogin(username, password);
//
//    }
}