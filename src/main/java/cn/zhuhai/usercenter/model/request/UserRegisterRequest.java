package cn.zhuhai.usercenter.model.request;

import lombok.Data;

/**
 * 用户注册请求体
 */
@Data
public class UserRegisterRequest {
    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 用户密码
     */
    private String userPassword;
    /**
     * 确认密码
     */
    private String checkPassword;
}
