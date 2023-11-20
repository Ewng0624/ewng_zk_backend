package cn.zhuhai.usercenter.service.impl;

import cn.zhuhai.usercenter.common.ErrorCode;
import cn.zhuhai.usercenter.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.zhuhai.usercenter.model.domain.User;
import cn.zhuhai.usercenter.service.UserService;
import cn.zhuhai.usercenter.mapper.UserMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.zhuhai.usercenter.constant.UserConstant.ADMIN_ROLE;
import static cn.zhuhai.usercenter.constant.UserConstant.USER_LOGIN_STATUS;

/**
* @author Ewng
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2023-11-14 12:03:27
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    /**
     * 盐值
     */
    public static final String SALT = "ewng";

    @Resource
    private UserMapper userMapper;

    /**
     * 用户注册表
     * @param userAccount 账号
     * @param userPassword 密码
     * @param checkPassword 确认密码
     * @return 用户id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.用户校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");
        }
        // 校验账户不包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\\\\\[\\\\\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }
        // 校验密码和确认密码是否相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不一致");
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.count(queryWrapper);
        // 数据库中已经存在
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已经被注册");
        }
        // 2.加密
        String newPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(newPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该用户已经存在");
        }
        return user.getId();
    }

    /**
     * 用户登录
     * @param userAccount 账号
     * @param userPassword 密码
     * @return
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            // todo 修改为自定义异常
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (userAccount.length() < 4 || userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号过短或密码过短");
        }
        // 校验账户不包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\\\\\[\\\\\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }
        // 加密
        String newPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 2.查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", newPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
        }
        // 3.脱敏
        User safetyUser = getSafeUser(user);
        // 4.记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATUS, safetyUser);
        log.info("SUCCESSFULL LOGIN");
        return safetyUser;
    }

    /**
     * 用户脱敏处理
     * @param user 用户信息
     * @return 脱敏后的用户
     */
    public User getSafeUser(User user) {
        User safetyUser = new User();
        if (safetyUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户为空");
        }
        safetyUser.setId(user.getId());
        safetyUser.setUsername(user.getUsername());
        safetyUser.setUserAccount(user.getUserAccount());
        safetyUser.setHeadUrl(user.getHeadUrl());
        safetyUser.setGender(user.getGender());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setUserRole(user.getUserRole());
        safetyUser.setUserStatus(user.getUserStatus());
        safetyUser.setCreateTime(user.getCreateTime());
        safetyUser.setTags(user.getTags());
        return safetyUser;
    }
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 符合条件的用户
     */
    @Override
    public List<User> searchUsers(String username) {
        if (StringUtils.isBlank(username)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("username", username);
        List<User> userList = this.list(queryWrapper);
        return userList;
    }

    /**
     * 更新用户
     * @param user 前端传递修改后的用户
     * @param loginUser 登录用户
     * @return
     */
    @Override
    public Integer updateUser(User user, User loginUser) {
        Long userId = user.getId();
        // 判断当前id是否为空
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 如果前端没有传递任何需要修改的值 则直接报错
        // 只有管理员和自己可以修改用户
        // 管理员修改任意用户  自己只能修改自己
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 根据传递的参数修改
        // todo 存入数据库是明文
        return userMapper.updateById(user);
    }


    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        // 获取当前用户的信息
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATUS);
        if (attribute == null) {
            return null;
        }
        User currentUser = (User) attribute;
        // 脱敏用户信息
        return getSafeUser(currentUser);
//        // 通过id从数据库中获取用户信息// todo 校验用户是否合法
//        Long id = currentUser.getId();
//        User user = userService.getById(id);
//        // 脱敏用户信息
//        User safeUser = userService.getSafeUser(user);

    }

    /**
     * 根据用户id删除用户
     * @param id 用户id
     * @return 成功/失败
     */
    @Override
    public boolean deleteUser(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id错误");
        }
        return this.removeById(id);
    }

    /**
     * 注销用户
     * @param request 请求
     * @return 是否成功注销
     */
    @Override
    public Integer userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATUS);
        return 1;
    }

    /**
     * 根据标签搜索用户
     * @param tagNameList 标签列表
     * @return 脱敏后的用户信息
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2. 在内存中判断是否包含要求的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafeUser).collect(Collectors.toList());
    }

    /**
     * 是否是管理员
     * @param request 请求
     * @return 是否是管理员
     */
    public Boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userAttribute = request.getSession().getAttribute(USER_LOGIN_STATUS);
        User user = (User) userAttribute;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 重载上面isAdmin的方法
     * @param loginUse 登录用户
     * @return
     */
    @Override
    public Boolean isAdmin(User loginUse) {
        return loginUse != null && loginUse.getUserRole() == ADMIN_ROLE;
    }
}




