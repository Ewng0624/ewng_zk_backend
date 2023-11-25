package cn.zhuhai.usercenter.model.dto.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author Ewng
 * @Description 退出队伍
 * @Date 2023/11/23 9:44
 */
@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = 3087570028880845789L;

    /**
     * 队伍id
     */
    private Long teamId;
}
