package com.xiaojin.auth.controller;


import com.atguigu.model.system.SysUser;
import com.atguigu.vo.system.SysUserQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaojin.auth.service.SysUserService;
import com.xiaojin.common.result.Result;
import com.xiaojin.common.utils.MD5;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author xiaojin
 * @since 2023-07-14
 */
@Api(tags = "用户管理")
@RestController
@CrossOrigin
@RequestMapping("/admin/system/sysUser")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @ApiOperation("用户条件分页查询")
    @GetMapping("{page}/{limit}")
    public Result<?> index(@PathVariable Long page,
                           @PathVariable Long limit,
                           SysUserQueryVo sysUserQueryVo){


        Page<SysUser> userPage = new Page<>(page, limit);

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        String username = sysUserQueryVo.getKeyword();
        String timeBegin = sysUserQueryVo.getCreateTimeBegin();
        String timeEnd = sysUserQueryVo.getCreateTimeEnd();

        if (!StringUtils.isEmpty(username)){
            queryWrapper.like(SysUser::getName,username);
        }
        if (!StringUtils.isEmpty(timeBegin)){
            queryWrapper.ge(SysUser::getCreateTime,timeBegin);
        }
        if (!StringUtils.isEmpty(timeEnd)){
            queryWrapper.le(SysUser::getCreateTime,timeEnd);
        }


        Page<SysUser> sysUserPage = sysUserService.page(userPage, queryWrapper);

        return Result.ok(sysUserPage);
    }

    @ApiOperation(value = "获取用户")
    @GetMapping("get/{id}")
    public Result<?> get(@PathVariable Long id){
        SysUser sysUser = sysUserService.getById(id);
        if (sysUser == null){
            return Result.fail().message("获取失败");
        }
        return Result.ok(sysUser);
    }


    @ApiOperation(value = "保存用户")
    @PostMapping("save")
    public Result<?> save(@RequestBody SysUser user){
        String password = user.getPassword();
        String encrypt = MD5.encrypt(password);
        user.setPassword(encrypt);
        boolean save = sysUserService.save(user);
        if (save){
            return Result.ok();
        }
        return Result.fail().message("保存失败");
    }

    @ApiOperation(value = "更新用户")
    @PutMapping("update")
    public Result<?> updateById(@RequestBody SysUser user){
        boolean update = sysUserService.updateById(user);
        if (update){
            return Result.ok();
        }
        return Result.fail().message("更新失败");

    }

    @ApiOperation(value = "删除用户")
    @DeleteMapping("remove/{id}")
    public Result<?> remove(@PathVariable Long id){
        boolean remove = sysUserService.removeById(id);
        if (remove){
            return Result.ok();
        }
        return Result.fail().message("删除失败");

    }
    @ApiOperation(value = "更新状态")
    @GetMapping("updateStatus/{id}/{status}")
    public Result<?> updateStatus(@PathVariable Long id, @PathVariable Integer status){
        sysUserService.updateStatus(id, status);
        return Result.ok();
    }

    @ApiOperation(value = "获取当前用户基本信息")
    @GetMapping("getCurrentUser")
    public Result<?> getCurrentUser() {
        return Result.ok(sysUserService.getCurrentUser());
    }


}

