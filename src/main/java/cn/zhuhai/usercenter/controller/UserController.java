package cn.zhuhai.usercenter.controller;

import cn.zhuhai.usercenter.common.BaseResponse;
import cn.zhuhai.usercenter.common.ErrorCode;
import cn.zhuhai.usercenter.common.ResultUtils;
import cn.zhuhai.usercenter.exception.BusinessException;
import cn.zhuhai.usercenter.model.domain.User;
import cn.zhuhai.usercenter.model.request.UserLoginRequest;
import cn.zhuhai.usercenter.model.request.UserRegisterRequest;
import cn.zhuhai.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(id);
    }

    /**
     * 用户登录
     * @param userLoginRequest 用户登录请求体
     * @param httpServletRequest 请求
     * @return 脱敏后的用户信息
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, httpServletRequest);
        return ResultUtils.success(user);
    }

    /**
     * 根据用户名称查询用户
     * @param username 用户名
     * @return 返回用户
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> userSearch(String username, HttpServletRequest request) {
        // 仅管理员可查询
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "仅管理员可查");
        }
        if (StringUtils.isBlank(username)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        List<User> userList = userService.searchUsers(username).stream()
                .map(user -> userService.getSafeUser(user))
                .collect(Collectors.toList());
        return ResultUtils.success(userList);
    }

    /**
     * 根据用户id删除用户
     * @param id 用户id
     * @return 是否成功删除
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        // 仅管理员可查询
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.deleteUser(id));
    }

    /**
     * 判断是否为管理员
     * @param request 请求
     * @return 是否为管理员
     */
    public Boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userAttribute = request.getSession().getAttribute(USER_LOGIN_STATUS);
        User user = (User) userAttribute;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 获取当前用户的登录状态
     * @param request 请求
     * @return 从数据库查找后的脱敏用户
     */
    @GetMapping("/current")
    public User getCurrentUser(HttpServletRequest request) {
        // 获取当前用户的信息
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATUS);
        User currentUser = (User) attribute;
        // 通过id从数据库中获取用户信息// todo 校验用户是否合法
        Long id = currentUser.getId();
        User user = userService.getById(id);
        // 脱敏用户信息
        User safeUser = userService.getSafeUser(user);
        return safeUser;
    }

    /**
     * 用户注销
     * @param request 请求
     * @return 成功/失败标识
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.NULL_ERROR);
        }
        // 删除当前登录状态
        return ResultUtils.success(userService.userLogout(request));
    }
}
