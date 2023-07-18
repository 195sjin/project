package com.xiaojin.auth.controller;


import com.atguigu.model.system.SysMenu;
import com.atguigu.vo.system.AssginMenuVo;
import com.xiaojin.auth.service.SysMenuService;
import com.xiaojin.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 菜单表 前端控制器
 * </p>
 *
 * @author xiaojin
 * @since 2023-07-14
 */
@Api(tags = "菜单管理")
@RestController
@CrossOrigin
@RequestMapping("/admin/system/sysMenu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    @ApiOperation(value = "获取菜单")
    @GetMapping("findNodes")
    public Result<?> findNodes(){
        List<SysMenu> list = sysMenuService.findNodes();
        if (StringUtils.isEmpty(list)){
            return Result.fail();
        }
        return Result.ok(list);

    }

    @ApiOperation(value = "新增菜单")
    @PostMapping("save")
    public Result<?> save(@RequestBody SysMenu permission){
        boolean save = sysMenuService.save(permission);
        if (save){
            return Result.ok();
        }
        return Result.fail();

    }

    @ApiOperation(value = "修改菜单")
    @PutMapping("update")
    public Result<?> updateById(@RequestBody SysMenu permission){
        boolean update = sysMenuService.updateById(permission);
        if (update){
            return Result.ok();
        }
        return Result.fail();

    }

    @ApiOperation(value = "删除菜单")
    @DeleteMapping("remove/{id}")
    public Result<?> remove(@PathVariable Long id){
        boolean remove = sysMenuService.removeMenuById(id);
        if (remove){
            return Result.ok();
        }
        return Result.fail();
    }


    @ApiOperation(value = "根据角色获取菜单")
    @GetMapping("toAssign/{roleId}")
    public Result<?> toAssign(@PathVariable Long roleId) {
        List<SysMenu> list = sysMenuService.findSysMenuByRoleId(roleId);
        return Result.ok(list);
    }

    @ApiOperation(value = "给角色分配权限")
    @PostMapping("/doAssign")
    public Result<?> doAssign(@RequestBody AssginMenuVo assignMenuVo) {
        sysMenuService.doAssign(assignMenuVo);
        return Result.ok();
    }

}

