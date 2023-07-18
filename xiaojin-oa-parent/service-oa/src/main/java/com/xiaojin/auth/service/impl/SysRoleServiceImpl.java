package com.xiaojin.auth.service.impl;


import com.atguigu.model.system.SysRole;
import com.atguigu.model.system.SysUserRole;
import com.atguigu.vo.system.AssginRoleVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaojin.auth.mapper.SysRoleMapper;
import com.xiaojin.auth.mapper.SysUserRoleMapper;
import com.xiaojin.auth.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    /**
     * 根据用户获取角色数据
     * @param userId
     * @return
     */
    @Override
    public Map<String, Object> findRoleByAdminId(Long userId) {

        //获取所有的角色
        List<SysRole> allRoleList = this.list();

        //获取当前用户的角色id
        List<SysUserRole> existUserRoleList = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .select(SysUserRole::getRoleId));
        List<Long> existRoleIdList = existUserRoleList.stream().map(c -> c.getRoleId()).collect(Collectors.toList());

        List<SysRole> assginRoleList = new ArrayList<>();
        for (SysRole role:allRoleList) {
            if (existRoleIdList.contains(role.getId())){
                assginRoleList.add(role);
            }
        }

        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("assginRoleList", assginRoleList);
        roleMap.put("allRolesList", allRoleList);

        return roleMap;
    }

    /**
     * 分配角色
     * @param assginRoleVo
     */
    @Override
    @Transactional
    public void doAssign(AssginRoleVo assginRoleVo) {
        //首先先删除中间表的数据
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId,assginRoleVo.getUserId()));

        //向表里面添加新的数据
        for (Long roleId:assginRoleVo.getRoleIdList()) {
            if (StringUtils.isEmpty(roleId)){
                continue;
            }
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(assginRoleVo.getUserId());
            userRole.setRoleId(roleId);
            sysUserRoleMapper.insert(userRole);
        }





    }
}
