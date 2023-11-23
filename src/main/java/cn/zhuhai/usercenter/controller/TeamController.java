package cn.zhuhai.usercenter.controller;

import cn.zhuhai.usercenter.common.BaseResponse;
import cn.zhuhai.usercenter.common.ErrorCode;
import cn.zhuhai.usercenter.common.ResultUtils;
import cn.zhuhai.usercenter.exception.BusinessException;
import cn.zhuhai.usercenter.model.domain.Team;
import cn.zhuhai.usercenter.model.domain.User;
import cn.zhuhai.usercenter.model.dto.TeamQuery;
import cn.zhuhai.usercenter.model.dto.request.TeamAddRequest;
import cn.zhuhai.usercenter.model.dto.request.TeamJoinRequest;
import cn.zhuhai.usercenter.model.dto.request.TeamUpdateRequest;
import cn.zhuhai.usercenter.model.vo.TeamUserVO;
import cn.zhuhai.usercenter.service.TeamService;
import cn.zhuhai.usercenter.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Ewng
 * @Date 2023/11/22 9:01
 */
@RestController
@RequestMapping("/team")
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    /**
     * 添加队伍
     * @param teamAddRequest 前端传递的队伍信息（JSON）
     * @return 添加成功的队伍ID
     */
    @PostMapping("/add")
    public BaseResponse<Long> teamAdd(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        try {
            BeanUtils.copyProperties(team, teamAddRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Long teamId = teamService.addTeam(team, request);
        return ResultUtils.success(teamId);
    }

    /**
     * 删除队伍
     * @param id 删除队伍的id
     * @return 成功 / 失败
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(Long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean remove = teamService.removeById(id);
        if (!remove) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "删除队伍失败");
        }
        return ResultUtils.success(true);
    }

//    /**
//     * 更新队伍
//     * @param team 前端传递更新后队伍参数
//     * @return 成功 / 失败
//     */
//    @PostMapping("/update")
//    public BaseResponse<Boolean> updateTeam(@RequestBody Team team) {
//        if (team == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        boolean result = teamService.updateById(team);
//        if (!result) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新队伍失败");
//        }
//        return ResultUtils.success(true);
//    }
    /**
     * 更新队伍
     * @param teamUpdateRequest 前端传递更新后队伍参数
     * @param request 请求
     * @return 成功 / 失败
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新队伍失败");
        }
        return ResultUtils.success(result);
    }

    /**
     * 根据id查找队伍
     * @param id 队伍id
     * @return 队伍信息
     */
    @GetMapping("/getTeamById")
    public BaseResponse<Team> getTeamById(Long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return ResultUtils.success(team);
    }

//    /**
//     * 根据队伍查询封装类查询队伍列表
//     * @param teamQuery 队伍查询封装类
//     * @return 队伍列表
//     */
//    @GetMapping("/getTeamsList")
//    public BaseResponse<List<Team>> getTeamsList(TeamQuery teamQuery) {
//        if (teamQuery == null) {
//            throw new BusinessException(ErrorCode.NULL_ERROR);
//        }
//        Team team = new Team();
//        try {
//            // 将teamQuery中的属性赋值到team中
//            BeanUtils.copyProperties(team, teamQuery);
//        } catch (Exception e) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
//        }
//        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
//        List<Team> teamList = teamService.list(queryWrapper);
//        return ResultUtils.success(teamList);
//    }

    /**
     * 查询队伍用户列表
     * @param teamQuery 队伍查询包装类
     * @param request 请求参数
     * @return 队伍用户包装类列表
     */
    @GetMapping("/getTeamList")
    public BaseResponse<List<TeamUserVO>> getTeamList(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Boolean isAdmin = userService.isAdmin(request);

        List<TeamUserVO> teamUserVOList = teamService.getTeamList(teamQuery, isAdmin);
        return ResultUtils.success(teamUserVOList);
    }

    /**
     * 分页逻辑
     * @param teamQuery 队伍查询封装类
     * @return 每页的队伍信息
     */
    @GetMapping("/getTeamsList/page")
    public BaseResponse<Page<Team>> listTeamByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Team team = new Team();
        try {
            BeanUtils.copyProperties(team, teamQuery);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> teamPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(teamPage);
    }

    /**
     * 加入队伍
     * @param teamJoinRequest 封装加入队伍请求
     * @param request 请求
     * @return 成功 / 失败
     */
    @PostMapping("joinTeam")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "加入队伍失败");
        }
        return ResultUtils.success(result);    }

}
