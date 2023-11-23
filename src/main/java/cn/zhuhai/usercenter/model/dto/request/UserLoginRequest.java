package cn.zhuhai.usercenter.model.dto.request;

import lombok.Data;

/**
 * 用户登录请求体
 */
@Data
public class UserLoginRequest {
    /**
     * 账号
     */
    private String userAccount;
    /**
     * 密码
     */
    private String userPassword;
}
