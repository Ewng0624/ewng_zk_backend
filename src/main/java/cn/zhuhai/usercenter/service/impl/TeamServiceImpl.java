package cn.zhuhai.usercenter.service.impl;
import java.util.Date;

import cn.zhuhai.usercenter.common.ErrorCode;
import cn.zhuhai.usercenter.common.ResultUtils;
import cn.zhuhai.usercenter.exception.BusinessException;
import cn.zhuhai.usercenter.model.domain.User;
import cn.zhuhai.usercenter.model.domain.UserTeam;
import cn.zhuhai.usercenter.model.dto.TeamQuery;
import cn.zhuhai.usercenter.model.dto.request.TeamJoinRequest;
import cn.zhuhai.usercenter.model.dto.request.TeamUpdateRequest;
import cn.zhuhai.usercenter.model.enums.TeamStatusEnum;
import cn.zhuhai.usercenter.model.vo.TeamUserVO;
import cn.zhuhai.usercenter.model.vo.UserVO;
import cn.zhuhai.usercenter.service.UserService;
import cn.zhuhai.usercenter.service.UserTeamService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.zhuhai.usercenter.model.domain.Team;
import cn.zhuhai.usercenter.service.TeamService;
import cn.zhuhai.usercenter.mapper.TeamMapper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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

    /**
     * 获取队伍信息
     * @param teamQuery 队伍查询包装类
     * @param isAdmin 是否为管理员
     * @return 队伍用户包装信息
     */
    @Override
    public List<TeamUserVO> getTeamList(TeamQuery teamQuery, Boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            // 根据id查询
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            //根据关键字查询
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                // 只要队伍名字和描述有一个就可查出来
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            // 根据队伍名称查询
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.eq("name", name);
            }
            // 根据队伍描述查询
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.eq("description", description);
            }
            // 根据队伍最大人数查询
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            // 根据创建人Id查询
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            // 根据队伍状态查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
            // 如果查询为空默认为公开
            if (teamStatusEnum == null) {
                teamStatusEnum = TeamStatusEnum.PUBLIC;
            }
            // 不是管理员 要查询非公开队伍
            if (!isAdmin && !teamStatusEnum.equals(TeamStatusEnum.PUBLIC)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            if (status != null && status > -1) {
                queryWrapper.eq("status", status);
            }
        }
        // 不展示已过期的队伍
        // expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt("expireTime", new Date())
                .or()
                .isNull("expireTime"));

        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        // 关联创建人信息
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team :teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            // 将team的属性值赋值给teamUserVO
            TeamUserVO teamUserVO = new TeamUserVO();
            try {
                BeanUtils.copyProperties(teamUserVO, team);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 将user的属性值赋值给userVO
            UserVO userVO = new UserVO();
            User user = userService.getById(userId);
            // 只有当用户不等于空的时候才去copy
            if (user != null) {
                try {
                    BeanUtils.copyProperties(userVO, user);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                teamUserVO.setUserVO(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }

        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断队伍是否存在
        Long id = teamUpdateRequest.getId();
        if (id == null || id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 当前用户不是登录用户 也不是管理员

        if (oldTeam.getUserId().longValue() != loginUser.getId().longValue() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 只有当新旧值不一样才进行更新
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        String name = teamUpdateRequest.getName();
        if (name != oldTeam.getName()) {
            queryWrapper.eq("name", name);
        }
        String description = teamUpdateRequest.getDescription();
        if (description != oldTeam.getDescription()) {
            queryWrapper.eq("description", description);
        }
        Date expireTime = teamUpdateRequest.getExpireTime();
        if (expireTime != oldTeam.getExpireTime()) {
            queryWrapper.eq("expireTime", expireTime);
        }
        // 不能修改成 0 - 2 以外的数字
        Integer status = teamUpdateRequest.getStatus();
        if (status != oldTeam.getStatus()) {
            if (status == null || status <= 0 || status > 2) {
                status = TeamStatusEnum.PUBLIC.getValue();
            }else {
                status = status == 1 ? TeamStatusEnum.PRIVATE.getValue() : TeamStatusEnum.SECRETE.getValue();
                // 当状态设置为密码时， 必须添加密码
                if (status == TeamStatusEnum.SECRETE.getValue()) {
                    if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                        throw new BusinessException(ErrorCode.NULL_ERROR, "必须设置密码");
                    }
                }
            }
            queryWrapper.eq("status", status);
        }
        String password = teamUpdateRequest.getPassword();
        if (password != oldTeam.getPassword()) {
            queryWrapper.eq("password", password);
        }
        Team updateTeam = new Team();
        try {
            BeanUtils.copyProperties(updateTeam, teamUpdateRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean update = this.updateById(updateTeam);
        return update;
    }

    @Override
    public Boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long teamJoinRequestId = teamJoinRequest.getId();
        if (teamJoinRequestId == null || teamJoinRequestId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 队伍必须存在
        Team team = this.getById(teamJoinRequestId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 不能加入已过期的队伍
        if (team.getExpireTime() != null && team.getExpireTime().before(new Date())) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍已经过期");
        }
        // 禁止加入私有队伍
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "禁止加入私有队伍");
        }
        // 如果队伍是加密的要密码匹配
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRETE.equals(teamStatusEnum)) {
            // 如果密码为空或者密码不匹配
            if (StringUtils.isBlank(password) || team.getPassword().equals(password)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码房密码错误");
            }
        }
        Long userId = loginUser.getId();
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        // 根据用户-队伍关联表进行查询
        long hasCountNum = userTeamService.count(queryWrapper);
        if (hasCountNum > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多可以创建5个队伍");
        }
        // 不能重复加入已加入的队伍
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("teamId", teamJoinRequestId);
        long hasUserJoinTeams = userTeamService.count(queryWrapper);
        if (hasUserJoinTeams > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "已加入，不能重复加入");
        }
        // 已加入队伍人数
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamJoinRequestId);
        long teamHasJoinNum = userTeamService.count(queryWrapper);
        if (teamHasJoinNum > team.getMaxNum()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "已达到队伍最大人数");
        }
        // 修改队伍信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamJoinRequestId);
        userTeam.setJoinTime(new Date());
        boolean save = userTeamService.save(userTeam);
        return save;
    }
}




