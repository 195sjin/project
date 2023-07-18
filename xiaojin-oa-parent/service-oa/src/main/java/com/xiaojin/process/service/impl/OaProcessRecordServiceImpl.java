package com.xiaojin.process.service.impl;

import com.atguigu.model.process.ProcessRecord;
import com.atguigu.model.system.SysUser;
import com.xiaojin.auth.service.SysUserService;
import com.xiaojin.process.mapper.OaProcessRecordMapper;
import com.xiaojin.process.service.OaProcessRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaojin.security.custom.LoginUserInfoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 审批记录 服务实现类
 * </p>
 *
 * @author xiaojin
 * @since 2023-07-18
 */
@Service
public class OaProcessRecordServiceImpl extends ServiceImpl<OaProcessRecordMapper, ProcessRecord> implements OaProcessRecordService {

    @Autowired
    private SysUserService sysUserService;

    @Resource
    private OaProcessRecordMapper processRecordMapper;

    @Override
    public void record(Long processId, Integer status, String description) {
        Long userId = LoginUserInfoHelper.getUserId();
        SysUser sysUser = sysUserService.getById(userId);

        ProcessRecord processRecord = new ProcessRecord();
        processRecord.setProcessId(processId);
        processRecord.setStatus(status);
        processRecord.setDescription(description);
        processRecord.setOperateUser(sysUser.getUsername());
        processRecord.setOperateUserId(userId);

        processRecordMapper.insert(processRecord);

    }
}
