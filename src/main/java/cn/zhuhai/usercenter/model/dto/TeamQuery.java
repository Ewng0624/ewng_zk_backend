package cn.zhuhai.usercenter.model.dto;

import cn.zhuhai.usercenter.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


/**
 * @Author Ewng
 * @Description 队伍查询封装类
 * @Date 2023/11/22 9:36
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeamQuery extends PageRequest {
    /**
     *
     */
    private Long id;

    /**
     * id列表
     */
    private List<Long> idList;

    /**
     * 关键字查询
     */
    private String searchText;

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
     * 用户Id
     */
    private Long userId;

    /**
     * 状态 0-公开 1-私有 2-加密
     */
    private Integer status;

}
