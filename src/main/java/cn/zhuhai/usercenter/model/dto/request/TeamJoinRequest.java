package cn.zhuhai.usercenter.model.dto.request;

import lombok.Data;

/**
 * @Author Ewng
 * @Description 封装加入队伍参数
 * @Date 2023/11/22 19:29
 */
@Data
public class TeamJoinRequest {
    /**
     * 加入队伍Id
     */
    private Long id;

    /**
     * 密码
     */
    private String password;

}
