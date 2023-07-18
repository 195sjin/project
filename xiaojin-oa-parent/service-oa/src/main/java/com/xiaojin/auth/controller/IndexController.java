package com.xiaojin.auth.controller;

import com.atguigu.model.system.SysUser;
import com.atguigu.vo.system.LoginVo;
import com.atguigu.vo.system.RouterVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaojin.auth.service.SysMenuService;
import com.xiaojin.auth.service.SysUserService;
import com.xiaojin.common.config.exception.GuiguException;
import com.xiaojin.common.jwt.JwtHelper;
import com.xiaojin.common.result.Result;
import com.xiaojin.common.utils.MD5;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author xiaojin
 * @Date 2023/7/13 17:57
 */
@Api(tags = "后台登录管理")
@RestController
@CrossOrigin
@RequestMapping("/admin/system/index")
public class IndexController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 登录
     * @return
     */
    @ApiOperation("登录")
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginVo loginVo){
        /*Map<String,Object> map = new HashMap<>();
        map.put("token","admin-token");
        return Result.ok(map);*/

        //根据用户名去数据库里面查找
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, loginVo.getUsername()));

        if (user == null){
            throw new GuiguException(201,"用户不存在");
        }

        //有的话比较密码
        //数据库里面的密码
        String password = user.getPassword();
        if (!password.equals(MD5.encrypt(loginVo.getPassword()))){
            throw new GuiguException(201,"密码错误");
        }

        //判断状态是否可用
        if (user.getStatus() == 0){
            throw new GuiguException(201,"用户被禁用");
        }

        //生成token
        String token = JwtHelper.createToken(user.getId(), user.getUsername());

        //返回token
        Map<String,Object> map = new HashMap<>();
        map.put("token",token);
        return Result.ok(map);
    }

    /**
     * 获取信息
     */
    @ApiOperation("获取信息")
    @GetMapping("/info")
    public Result<?> info(HttpServletRequest request){
        //获取用户信息
        String token = request.getHeader("token");
        Long userId = JwtHelper.getUserId(token);
        String username = JwtHelper.getUsername(token);

        SysUser sysUser = sysUserService.getById(userId);
        if (sysUser == null){
            throw new GuiguException(201,"用户信息不存在");
        }

        //获取用户菜单列表
        List<RouterVo> routerList = sysMenuService.findUserMenuList(userId);

        //获取用户可以操作的按钮列表
        List<String> permsList = sysMenuService.findPermsList(userId);




        Map<String,Object> map = new HashMap<>();
        map.put("roles","[admin]");
        map.put("name",username);
        map.put("avatar","https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
        map.put("buttons", permsList);
        map.put("routers", routerList);
        return Result.ok(map);
    }




    /**
     * 退出
     */
    @ApiOperation("退出")
    @PostMapping("/logout")
    public Result<?> logout(){
        return Result.ok();
    }
}
