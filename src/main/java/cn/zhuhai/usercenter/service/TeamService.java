package cn.zhuhai.usercenter.service;

import cn.zhuhai.usercenter.model.domain.Team;
import cn.zhuhai.usercenter.model.domain.User;
import cn.zhuhai.usercenter.model.dto.TeamQuery;
import cn.zhuhai.usercenter.model.dto.request.TeamJoinRequest;
import cn.zhuhai.usercenter.model.dto.request.TeamUpdateRequest;
import cn.zhuhai.usercenter.model.vo.TeamUserVO;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    /**
     * 根据队伍信息包装类获取队伍用户包装类信息
     * @param teamQuery 队伍查询包装类
     * @param isAdmin 是否为管理员
     * @return 队伍用户信息包装类列表
     */
    List<TeamUserVO> getTeamList(TeamQuery teamQuery, Boolean isAdmin);

    /**
     * 修改队伍信息
     * @param teamUpdateRequest 前端传递team（JSON）
     * @param loginUser 当前登录用户
     * @return 是否修改成功
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);


    /**
     * 加入队伍
     * @param teamJoinRequest 封装加入队伍请求
     * @param loginUser 当前登录用户
     * @return 成功 / 失败
     */
    Boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);
}
