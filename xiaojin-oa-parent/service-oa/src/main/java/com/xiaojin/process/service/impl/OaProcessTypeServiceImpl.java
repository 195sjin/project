package com.xiaojin.process.service.impl;

import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.process.ProcessType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaojin.process.mapper.OaProcessTemplateMapper;
import com.xiaojin.process.mapper.OaProcessTypeMapper;
import com.xiaojin.process.service.OaProcessTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author xiaojin
 * @since 2023-07-17
 */
@Service
public class OaProcessTypeServiceImpl extends ServiceImpl<OaProcessTypeMapper, ProcessType> implements OaProcessTypeService {

    @Autowired
    private OaProcessTypeMapper processTypeMapper;

    @Autowired
    private OaProcessTemplateMapper processTemplateMapper;

    @Override
    public List<ProcessType> findProcessType() {
        //查询所有数据
        List<ProcessType> processTypeList = processTypeMapper.selectList(null);

        //遍历，根据集合中的id去获取审批模板
        for (ProcessType processType:processTypeList) {
            Long typeId = processType.getId();
            List<ProcessTemplate> processTemplateList = processTemplateMapper.selectList(new LambdaQueryWrapper<ProcessTemplate>().eq(ProcessTemplate::getProcessTypeId, typeId));

            //封装
            processType.setProcessTemplateList(processTemplateList);
        }

        return processTypeList;
    }
}
