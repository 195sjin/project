package com.xiaojin.auth.controller;

import com.atguigu.model.system.SysRole;
import com.atguigu.vo.system.AssginRoleVo;
import com.atguigu.vo.system.SysRoleQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaojin.auth.service.SysRoleService;
import com.xiaojin.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Author xiaojin
 * @Date 2023/7/13 11:19
 */
@Api(tags = "角色管理")
@RestController
@CrossOrigin
@RequestMapping("/admin/system/sysRole")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @ApiOperation(value = "获取全部角色列表")
    @GetMapping("/findAll")
    public Result<?> findAll(){
        List<SysRole> list = sysRoleService.list();
        return Result.ok(list);
    }

    /**
     * 条件分页查询
     */
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("条件分页查询")
    @GetMapping("/{page}/{limit}")
    public Result<?> pageQueryRole(@PathVariable Long page,
                                   @PathVariable Long limit,
                                   SysRoleQueryVo sysRoleQueryVo){


        Page<SysRole> objectPage = new Page<>(page, limit);

        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        String roleName = sysRoleQueryVo.getRoleName();
        if (!StringUtils.isEmpty(roleName)){
            queryWrapper.like(SysRole::getRoleName,roleName);
        }

        Page<SysRole> sysRolePage = sysRoleService.page(objectPage, queryWrapper);
        return Result.ok(sysRolePage);
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.add')")
    @ApiOperation("添加角色")
    @PostMapping("/save")
    public Result<?> save(@RequestBody SysRole sysRole){
        boolean save = sysRoleService.save(sysRole);
        if (save){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("根据id查询")
    @GetMapping("/get/{id}")
    public Result<?> get(@PathVariable Long id){
        SysRole sysRole = sysRoleService.getById(id);
        if (sysRole == null){
            return Result.fail();
        }
        return Result.ok(sysRole);
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.update')")
    @ApiOperation("修改角色")
    @PutMapping("/update")
    public Result<?> update(@RequestBody SysRole sysRole){
        boolean b = sysRoleService.updateById(sysRole);
        if (b){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("根据id删除")
    @DeleteMapping("/remove/{id}")
    public Result<?> deleteById(@PathVariable Long id){
        boolean remove = sysRoleService.removeById(id);
        if (remove){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("批量删除")
    @DeleteMapping("/batchRemove")
    public Result<?> delete(@RequestBody List<Long> idList){
        boolean remove = sysRoleService.removeByIds(idList);
        if (remove){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    @ApiOperation("查询所有角色和当前用户所属角色")
    @GetMapping("/toAssign/{userId}")
    public Result<?> toAssign(@PathVariable Long userId){
        Map<String, Object> roleMap = sysRoleService.findRoleByAdminId(userId);
        return Result.ok(roleMap);
    }


    @ApiOperation("为用户分配角色")
    @PostMapping("/doAssign")
    public Result<?> doAssign(@RequestBody AssginRoleVo assginRoleVo){
        sysRoleService.doAssign(assginRoleVo);
        return Result.ok();

    }



}
