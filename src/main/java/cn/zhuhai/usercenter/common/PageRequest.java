package cn.zhuhai.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author Ewng
 * @Description 通用分页请求参数
 * @Date 2023/11/22 9:55
 */
@Data
public class PageRequest implements Serializable {


    private static final long serialVersionUID = -8488649266526888965L;
    /**
     * 页面大小 -- 默认一页10条
     */
    protected int pageSize = 10;

    /**
     * 当前第几页 -- 默认第一页
     */
    protected int pageNum = 1;
}
