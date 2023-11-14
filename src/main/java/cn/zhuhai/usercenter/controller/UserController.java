package cn.zhuhai.usercenter.controller;

import cn.zhuhai.usercenter.model.domain.User;
import cn.zhuhai.usercenter.model.request.UserLoginRequest;
import cn.zhuhai.usercenter.model.request.UserRegisterRequest;
import cn.zhuhai.usercenter.service.UserService;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cn.zhuhai.usercenter.constant.UserConstant.ADMIN_ROLE;
import static cn.zhuhai.usercenter.constant.UserConstant.USER_LOGIN_STATUS;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterRequest 用户注册请求体
     * @return  用户id
     */
    @PostMapping("/register")
    public Long userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            return null;
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long id = userService.userRegister(userAccount, userPassword, checkPassword);
        return id;
    }

    /**
     * 用户登录
     * @param userLoginRequest 用户登录请求体
     * @param httpServletRequest 请求
     * @return 脱敏后的用户信息
     */
    @PostMapping("/login")
    public User userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest) {
        if (userLoginRequest == null) {
            return null;
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        User user = userService.userLogin(userAccount, userPassword, httpServletRequest);
        return userService.getSafeUser(user);
    }

    /**
     * 根据用户名称查询用户
     * @param username 用户名
     * @return 返回用户
     */
    @GetMapping("/search")
    public List<User> userSearch(String username, HttpServletRequest request) {
        // 仅管理员可查询
        if (!isAdmin(request)) {
            return new ArrayList<>();
        }
        if (StringUtils.isBlank(username)) {
            return new ArrayList<>();
        }
        List<User> userList = userService.searchUsers(username).stream()
                .map(user -> userService.getSafeUser(user))
                .collect(Collectors.toList());
        return userList;
    }

    /**
     * 根据用户id删除用户
     * @param id 用户id
     * @return 是否成功删除
     */
    @PostMapping("/delete")
    public boolean deleteUser(@RequestBody long id, HttpServletRequest request) {
        // 仅管理员可查询
        if (!isAdmin(request)) {
            return false;
        }
        if (id <= 0) {
            return false;
        }
        return userService.deleteUser(id);
    }

    /**
     * 判断是否为管理员
     * @param request 请求
     * @return 是否为管理员
     */
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userAttribute = request.getSession().getAttribute(USER_LOGIN_STATUS);
        User user = (User) userAttribute;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }
}
