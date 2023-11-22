package cn.zhuhai.usercenter.model.dto.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author Ewng
 * @Description 允许用户修改的队伍信息包装类
 * @Date 2023/11/22 16:53
 */
@Data
public class TeamUpdateRequest implements Serializable {

    private static final long serialVersionUID = 8839012157560143657L;

    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 状态 0-公开 1-私有 2-加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;
}
