package cn.zhuhai.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.zhuhai.usercenter.model.domain.UserTeam;
import cn.zhuhai.usercenter.service.UserTeamService;
import cn.zhuhai.usercenter.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author Ewng
* @description 针对表【user_team(用户-队伍关系)】的数据库操作Service实现
* @createDate 2023-11-22 08:47:32
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




