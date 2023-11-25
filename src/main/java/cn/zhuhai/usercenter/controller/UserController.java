package cn.zhuhai.usercenter.controller;

import cn.zhuhai.usercenter.common.BaseResponse;
import cn.zhuhai.usercenter.common.ErrorCode;
import cn.zhuhai.usercenter.common.ResultUtils;
import cn.zhuhai.usercenter.exception.BusinessException;
import cn.zhuhai.usercenter.model.domain.User;
import cn.zhuhai.usercenter.model.dto.request.UserLoginRequest;
import cn.zhuhai.usercenter.model.dto.request.UserRegisterRequest;
import cn.zhuhai.usercenter.model.vo.UserVO;
import cn.zhuhai.usercenter.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.zhuhai.usercenter.constant.UserConstant.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

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
        if (userService.isAdmin(request)) {
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
     * 根据标签查找用户
     * @param tagNameList 标签列表
     * @return 用户
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
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
        if (userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.deleteUser(id));
    }

    /**
     * 修改用户
     * @param user 前端传递修改后的用户（JSON格式）
     * @param request 请求
     * @return 返回更细成功数字
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 1.判断参数
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.鉴权是否有权限，是否为本人
        // 3.更新操作
        User loginUser = userService.getLoginUser(request);
        // 未登录
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Integer result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录的用户
     * @param request 请求
     * @return 从数据库查找后的脱敏用户
     */
    @GetMapping("/current")
    public User getCurrentUser(HttpServletRequest request) {
        return userService.getLoginUser(request);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUser(int pageSize, int pageNum, HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        String redisKey = String.format("%s:user:recommend:%s", REDIS_KEY_PREFIX, userId);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        // 如果有缓存直接读取缓存
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }
        // 没有缓存,查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 数据少用列表全部加载
        //List<User> userList = userService.list(queryWrapper);
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        // 写缓存
        try {
            valueOperations.set(redisKey, userPage, 1, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis set key error");
        }
        return ResultUtils.success(userPage);
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

    /**
     * 推荐用户（top N）
     * @param nums 推荐num条
     * @param request 请求
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long nums, HttpServletRequest request) {
        if (nums <= 0 || nums > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(nums, loginUser));
    }
}
