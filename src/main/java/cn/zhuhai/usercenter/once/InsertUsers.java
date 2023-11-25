package cn.zhuhai.usercenter.once;

import cn.zhuhai.usercenter.mapper.UserMapper;
import cn.zhuhai.usercenter.model.domain.User;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author Ewng
 * @Date 2023/11/20 15:30
 */
@Component
public class InsertUsers {
    @Resource
    private UserMapper userMapper;

    /**
     * 批量插入数据
     */
    public void doInsertUsers() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            User user = new User();
            user.setUsername("假用户"+i);
            user.setUserAccount("fakeEwng");
            user.setHeadUrl("");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123");
            user.setEmail("456");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setTags("[]");
            userMapper.insert(user);
        }
        long end = System.currentTimeMillis();
        System.out.println(end-start);
    }
}
