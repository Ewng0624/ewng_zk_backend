package cn.zhuhai.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

/**
 * 用户
 * @TableName user
 */
@Data
public class User implements Serializable {

    private static final long serialVersionUID = -7191054483240186091L;
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * '用户昵称'
     */
    private String username;

    /**
     * '用户账号'
     */
    private String userAccount;

    /**
     * '用户头像'
     */
    private String headUrl;

    /**
     * '性别'
     */
    private Integer gender;

    /**
     * '密码'
     */
    private String userPassword;

    /**
     * '电话'
     */
    private String phone;

    /**
     * '邮箱'
     */
    private String email;

    /**
     * 状态 0 - 正常
     */
    private Integer userStatus;

    /**
     * 用户角色 0-普通用户 1-管理员
     */
    private Integer userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除（逻辑删除）
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 标签列表 json
     */
    private String tags;

}