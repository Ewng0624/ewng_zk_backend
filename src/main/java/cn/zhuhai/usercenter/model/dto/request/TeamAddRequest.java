package cn.zhuhai.usercenter.model.dto.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author Ewng
 * @Description 添加队伍请求参数封装
 * @Date 2023/11/22 11:36
 */
@Data
public class TeamAddRequest implements Serializable {

    private static final long serialVersionUID = -5580826897462271166L;
    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 状态 0-公开 1-私有 2-加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;
}
