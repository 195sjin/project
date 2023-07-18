package com.xiaojin.process.service.impl;

import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.process.ProcessType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaojin.process.mapper.OaProcessTemplateMapper;
import com.xiaojin.process.mapper.OaProcessTypeMapper;
import com.xiaojin.process.service.OaProcessService;
import com.xiaojin.process.service.OaProcessTemplateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 审批模板 服务实现类
 * </p>
 *
 * @author xiaojin
 * @since 2023-07-17
 */
@Service
public class OaProcessTemplateServiceImpl extends ServiceImpl<OaProcessTemplateMapper, ProcessTemplate> implements OaProcessTemplateService {

    @Resource
    private OaProcessTemplateMapper processTemplateMapper;

    @Resource
    private OaProcessTypeMapper processTypeMapper;

    @Resource
    private OaProcessService processService;

    @Override
    @Transactional
    public Page<ProcessTemplate> selectPage(Page<ProcessTemplate> pageParam) {
        //进行分页查询
        LambdaQueryWrapper<ProcessTemplate> queryWrapper = new LambdaQueryWrapper<ProcessTemplate>();
        queryWrapper.orderByDesc(ProcessTemplate::getId);

        Page<ProcessTemplate> page = processTemplateMapper.selectPage(pageParam, queryWrapper);

        //获取分页查询到的每一条数据
        List<ProcessTemplate> processTemplateList = page.getRecords();

        //获取每条数据的类型id
        List<Long> processTypeList = processTemplateList.stream().map(processTemplate -> processTemplate.getProcessTypeId()).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(processTemplateList)){
            //根据类型id集合查出来类型id对应的类型实体封装成map集合
            Map<Long, ProcessType> processTypeIdToProcessTypeMap = processTypeMapper.selectList(new LambdaQueryWrapper<ProcessType>().in(ProcessType::getId,processTypeList))
                    .stream().collect(Collectors.toMap(ProcessType::getId,processType -> processType));

            for (ProcessTemplate processTemplate:processTemplateList) {
                ProcessType processType = processTypeIdToProcessTypeMap.get(processTemplate.getProcessTypeId());
                if (processType == null){
                    continue;
                }
                processTemplate.setProcessTypeName(processType.getName());
            }

        }
        return page;

    }

    @Override
    @Transactional
    public void publish(Long id) {
        ProcessTemplate processTemplate = processTemplateMapper.selectById(id);
        processTemplate.setStatus(1);
        processTemplateMapper.updateById(processTemplate);

        //流程定义部署
        if(!StringUtils.isEmpty(processTemplate.getProcessDefinitionPath())) {
            processService.deployByZip(processTemplate.getProcessDefinitionPath());
        }

    }
}
