package com.xiaojin.auth.service.impl;



import com.atguigu.model.system.SysUser;
import com.xiaojin.auth.service.SysMenuService;
import com.xiaojin.auth.service.SysUserService;
import com.xiaojin.security.custom.CustomUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysMenuService sysMenuService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserService.getByUsername(username);
        if(null == sysUser) {
            throw new UsernameNotFoundException("用户名不存在！");
        }

        if(sysUser.getStatus() == 0) {
            throw new RuntimeException("账号已停用");
        }

        //根据用户id查询用户权限的数据
        List<String> permsList = sysMenuService.findPermsList(sysUser.getId());
        //封装数据
        List<SimpleGrantedAuthority> authList = new ArrayList<>();
        for (String perm:permsList) {
            authList.add(new SimpleGrantedAuthority(perm.trim()));
        }
        return new CustomUser(sysUser, authList);
    }
}
