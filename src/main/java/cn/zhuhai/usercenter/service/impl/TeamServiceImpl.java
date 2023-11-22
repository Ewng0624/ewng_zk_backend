package cn.zhuhai.usercenter.service.impl;

import cn.zhuhai.usercenter.common.ErrorCode;
import cn.zhuhai.usercenter.common.ResultUtils;
import cn.zhuhai.usercenter.exception.BusinessException;
import cn.zhuhai.usercenter.model.domain.User;
import cn.zhuhai.usercenter.model.domain.UserTeam;
import cn.zhuhai.usercenter.model.enums.TeamStatusEnum;
import cn.zhuhai.usercenter.service.UserService;
import cn.zhuhai.usercenter.service.UserTeamService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.zhuhai.usercenter.model.domain.Team;
import cn.zhuhai.usercenter.service.TeamService;
import cn.zhuhai.usercenter.mapper.TeamMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;

/**
* @author Ewng
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2023-11-22 08:45:01
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;

    /**
     * 添加队伍
     * @param team 前端传递添加队伍信息
     * @param request 请求参数
     * @return 队伍id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long addTeam(Team team, HttpServletRequest request) {
        // 请求参数为空
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 用户是否登录
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 队伍最大人数
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不符合要求");
        }
        // 队伍名字
        String name = team.getName();
        if (StringUtils.isBlank(name) ||name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不符合要求");
        }
        // 队伍描述
        String description = team.getDescription();
        // 描述可以为空
        // 传递描述才判断描述的长度
        if (StringUtils.isNotBlank(description) && team.getDescription().length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        // status是否公开(int) 不传默认为 0 公开
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        // 加密要有密码
        String password = team.getPassword();
        if (TeamStatusEnum.SECRETE.equals(statusEnum) && (StringUtils.isBlank(password) || password.length() > 20)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
        }
        // 超时时间大于当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }
        // 校验队伍最多创建5个队伍
        Long userId = loginUser.getId();
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 在队伍表中查找创建队伍用户
        // todo 有bug 如果在瞬间点击100次会创建100个队伍
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍过多");
        }
        // 用事务保证插入到 队伍表 和 用户队伍关系表 是一条原子性操作
        // 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean save = this.save(team);
        if (!save) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍队伍失败");
        }
        // 将队伍信息同步到用户-队伍关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(new Date());
        save = userTeamService.save(userTeam);
        if (!save) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return team.getId();
    }
}




