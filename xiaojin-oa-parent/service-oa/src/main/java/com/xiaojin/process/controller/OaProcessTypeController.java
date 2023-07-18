package com.xiaojin.process.controller;


import com.atguigu.model.process.ProcessType;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaojin.common.result.Result;
import com.xiaojin.process.service.OaProcessTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 审批类型 前端控制器
 * </p>
 *
 * @author xiaojin
 * @since 2023-07-17
 */
@Api(value = "审批类型", tags = "审批类型")
@RestController
@CrossOrigin
@RequestMapping("/admin/process/processType")
public class OaProcessTypeController {
    @Autowired
    private OaProcessTypeService oaProcessTypeService;

    @PreAuthorize("hasAuthority('bnt.processType.list')")
    @ApiOperation(value = "获取分页列表")
    @GetMapping("/{page}/{limit}")
    public Result<?> index(@PathVariable Long page,
                        @PathVariable Long limit){
        Page<ProcessType> typePage = oaProcessTypeService.page(new Page<ProcessType>(page, limit));
        return Result.ok(typePage);

    }

    @PreAuthorize("hasAuthority('bnt.processType.list')")
    @ApiOperation(value = "获取")
    @GetMapping("/get/{id}")
    public Result<?> get(@PathVariable Long id){
        ProcessType byId = oaProcessTypeService.getById(id);
        return Result.ok(byId);
    }

    @PreAuthorize("hasAuthority('bnt.processType.add')")
    @ApiOperation(value = "新增")
    @PostMapping("/save")
    public Result<?> save(@RequestBody ProcessType processType){
        oaProcessTypeService.save(processType);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.processType.update')")
    @ApiOperation(value = "修改")
    @PutMapping("/update")
    public Result<?> updateById(@RequestBody ProcessType processType){
        oaProcessTypeService.updateById(processType);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.processType.remove')")
    @ApiOperation(value = "删除")
    @DeleteMapping("/remove/{id}")
    public Result<?> remove(@PathVariable Long id){
        oaProcessTypeService.removeById(id);
        return Result.ok();
    }
    @ApiOperation(value = "获取全部审批分类")
    @GetMapping("findAll")
    public Result<?> findAll() {
        return Result.ok(oaProcessTypeService.list());
    }

}

