package cn.zhuhai.usercenter.controller;

import cn.zhuhai.usercenter.common.BaseResponse;
import cn.zhuhai.usercenter.common.ErrorCode;
import cn.zhuhai.usercenter.common.ResultUtils;
import cn.zhuhai.usercenter.exception.BusinessException;
import cn.zhuhai.usercenter.model.domain.Team;
import cn.zhuhai.usercenter.model.domain.User;
import cn.zhuhai.usercenter.model.dto.TeamQuery;
import cn.zhuhai.usercenter.model.dto.request.TeamAddRequest;
import cn.zhuhai.usercenter.service.TeamService;
import cn.zhuhai.usercenter.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
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

    /**
     * 更新队伍
     * @param team 前端传递更新后队伍参数
     * @return 成功 / 失败
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody Team team) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.updateById(team);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新队伍失败");
        }
        return ResultUtils.success(true);
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

    /**
     * 根据队伍查询封装类查询队伍列表
     * @param teamQuery 队伍查询封装类
     * @return 队伍列表
     */
    @GetMapping("/getTeamsList")
    public BaseResponse<List<Team>> getTeamsList(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Team team = new Team();
        try {
            // 将teamQuery中的属性赋值到team中
            BeanUtils.copyProperties(team, teamQuery);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        List<Team> teamList = teamService.list(queryWrapper);
        return ResultUtils.success(teamList);
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
     * 根据名字查询队伍
     * @param name 队伍名字
     * @return 队伍信息
     */
    @GetMapping("/getTeamByName")
    public BaseResponse<Team> getTeamByName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        Team team = teamService.getOne(queryWrapper);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return ResultUtils.success(team);
    }

}
