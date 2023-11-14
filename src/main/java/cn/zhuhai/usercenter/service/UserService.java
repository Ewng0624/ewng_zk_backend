package cn.zhuhai.usercenter.service;

import cn.zhuhai.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
* @author Ewng
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2023-11-14 12:03:27
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount 账号
     * @param password 密码
     * @param checkPassword 确认密码
     * @return 用户id
     */
    long userRegister(String userAccount, String password, String checkPassword);

    /**
     * 用户登录
     * @param userAccount 账号
     * @param password 密码
     * @return 用户信息
     */
    User userLogin(String userAccount, String password, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param user 完整用户
     * @return 处理后的脱敏用户
     */
    User getSafeUser(User user);
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户列表
     */
    List<User> searchUsers(String username);

    /**
     * 根据用户id删除用户
     * @param id 用户id
     * @return 成功/失败
     */
    public boolean deleteUser(long id);
}
