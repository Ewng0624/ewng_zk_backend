package cn.zhuhai.usercenter.service;

import cn.zhuhai.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author Ewng
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2023-11-22 08:45:01
*/
public interface TeamService extends IService<Team> {
    /**
     * 添加队伍
     * @param team 前端传递添加队伍信息
     * @return 添加成功后的队伍id
     */
    Long addTeam(Team team, HttpServletRequest request);
}
