package com.xiaojin.process.service.impl;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.atguigu.model.process.Process;
import com.atguigu.model.process.ProcessRecord;
import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.system.SysUser;
import com.atguigu.vo.process.ApprovalVo;
import com.atguigu.vo.process.ProcessFormVo;
import com.atguigu.vo.process.ProcessQueryVo;
import com.atguigu.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaojin.auth.service.SysUserService;
import com.xiaojin.process.mapper.OaProcessMapper;
import com.xiaojin.process.service.MessageService;
import com.xiaojin.process.service.OaProcessRecordService;
import com.xiaojin.process.service.OaProcessService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaojin.process.service.OaProcessTemplateService;
import com.xiaojin.security.custom.LoginUserInfoHelper;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.activiti.engine.task.Task;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author xiaojin
 * @since 2023-07-17
 */
@Service
public class OaProcessServiceImpl extends ServiceImpl<OaProcessMapper, Process> implements OaProcessService {

    @Resource
    private OaProcessMapper processMapper;

    @Resource
    private RepositoryService repositoryService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private OaProcessTemplateService processTemplateService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private OaProcessRecordService processRecordService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private MessageService messageService;

    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo) {
        IPage<ProcessVo> page = processMapper.selectPage(pageParam, processQueryVo);
        return page;
    }

    @Override
    public void deployByZip(String deployPath) {
        // 定义zip输入流
        InputStream inputStream = this
                .getClass()
                .getClassLoader()
                .getResourceAsStream(deployPath);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        // 流程部署
        Deployment deployment = repositoryService.createDeployment()
                .addZipInputStream(zipInputStream)
                .deploy();
    }

    @Override
    public void startUp(ProcessFormVo processFormVo) {
        //根据当前用户id获取信息
        SysUser sysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());

        //根据审批模板id获取模板信息
        ProcessTemplate processTemplate = processTemplateService.getById(processFormVo.getProcessTemplateId());

        //保存提交的信息到业务表 oa_process
        Process process = new Process();
        BeanUtils.copyProperties(processFormVo,process);
        String workNo = System.currentTimeMillis() + "";
        process.setProcessCode(workNo);
        process.setUserId(LoginUserInfoHelper.getUserId());
        process.setFormValues(processFormVo.getFormValues());
        process.setTitle(sysUser.getName() + "发起" + processTemplate.getName() + "申请");
        process.setStatus(1);

        processMapper.insert(process);

        //启动流程实例
        //1.流程定义key
        String processDefinitionKey = processTemplate.getProcessDefinitionKey();
        //2.业务key processId
        String processId = String.valueOf(process.getId());
        //3.流程参数from表单JSON数据，转map集合
        String formValues = processFormVo.getFormValues();
        JSONObject jsonObject = JSON.parseObject(formValues);
        JSONObject formData = jsonObject.getJSONObject("formData");

        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String,Object> entry:formData.entrySet()) {
            map.put(entry.getKey(),entry.getValue());
        }
        Map<String, Object> map2 = new HashMap<>();
        map2.put("data",map);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, processId, map2);

        //查询下一个审批人
        List<Task> taskList = this.getCurrentTaskList(processInstance.getId());
        List<String> nameList = new ArrayList<>();
        for (Task task : taskList){
            String assigneeName = task.getAssignee();
            SysUser user = sysUserService.getByUsername(assigneeName);
            String name = user.getName();
            nameList.add(name);
            //TODO 推送消息
            messageService.pushPendingMessage(process.getId(), sysUser.getId(), task.getId());
        }

        process.setProcessInstanceId(processInstance.getId());
        process.setDescription("等待" + StringUtils.join(nameList.toArray(), ",") + "审批");
        //业务和流程关联 更新oa_process数据
        processMapper.updateById(process);

        //记录操作审批信息记录
        processRecordService.record(process.getId(),1,"发起申请");


    }

    @Override
    public IPage<ProcessVo> findPending(Page<java.lang.Process> pageParam) {
        //封装查询条件，根据当前登录的用户名称
        TaskQuery query = taskService.createTaskQuery().taskAssignee(LoginUserInfoHelper.getUsername()).orderByTaskCreateTime().desc();

        //调用分页查询，返回list集合，待办任务集合
        //（前页-1）*每页显示的记录数，就是开始位置
        int begin = (int) ((pageParam.getCurrent() - 1)*pageParam.getSize());
        int size = (int) pageParam.getCurrent();
        List<Task> taskList = query.listPage(begin, size);
        long count = query.count();

        //封装返回list集合数据，到list<ProcessVo>里面
        List<ProcessVo> processVoList = new ArrayList<>();
        for (Task task : taskList){
            //从task获取流程实例id
            String processInstanceId = task.getProcessInstanceId();

            //根据流程实例id获取实例对象
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

            //从流程实例对象获取业务key
            String businessKey = processInstance.getBusinessKey();
            if (businessKey == null){
                continue;
            }

            //根据业务key获取process对象
            long parseId = Long.parseLong(businessKey);
            Process process = processMapper.selectById(parseId);

            //把process转化为processVo
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process,processVo);
            processVo.setTaskId(task.getId());

            processVoList.add(processVo);
        }

        //返回IPage对象
        Page<ProcessVo> page = new Page<>(pageParam.getCurrent(), pageParam.getSize(), count);
        page.setRecords(processVoList);
        return page;
    }

    @Override
    public Map<String, Object> show(Long id) {

        Process process = this.getById(id);
        List<ProcessRecord> processRecordList = processRecordService.list(new LambdaQueryWrapper<ProcessRecord>().eq(ProcessRecord::getProcessId, id));
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        Map<String, Object> map = new HashMap<>();
        map.put("process", process);
        map.put("processRecordList", processRecordList);
        map.put("processTemplate", processTemplate);
        //计算当前用户是否可以审批，能够查看详情的用户不是都能审批，审批后也不能重复审批
        boolean isApprove = false;
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if (!CollectionUtils.isEmpty(taskList)) {
            for(Task task : taskList) {
                if(task.getAssignee().equals(LoginUserInfoHelper.getUsername())) {
                    isApprove = true;
                }
            }
        }
        map.put("isApprove", isApprove);
        return map;
    }

    @Override
    public void approve(ApprovalVo approvalVo) {
        //从vo里面获取任务id，根据任务id获取流程变量
        String taskId = approvalVo.getTaskId();
        Map<String, Object> variables = taskService.getVariables(taskId);

        //判断审批状态值
        if (approvalVo.getStatus() == 1){
            //通过
            Map<String,Object> map = new HashMap<>();
            taskService.complete(taskId,map);
        }else {
            //驳回，流程结束
            this.endTask(taskId);
        }

        //记录审批相关过程信息，oa_process_record
        String description = approvalVo.getStatus() == 1 ? "已通过" : "驳回";
        processRecordService.record(approvalVo.getProcessId(),approvalVo.getStatus(),description);

        //查询下一个审批人，更新流程表记录 process表记录
        Process process = processMapper.selectById(approvalVo.getProcessId());
        //查询任务
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if (!CollectionUtils.isEmpty(taskList)){
            List<String> assignList = new ArrayList<>();
            for (Task task : taskList){
                String assignee = task.getAssignee();
                SysUser sysUser = sysUserService.getByUsername(assignee);
                assignList.add(sysUser.getName());

                //TODO 公众号消息推送
                messageService.pushPendingMessage(process.getId(), sysUser.getId(), task.getId());
            }
            process.setDescription("等待" + StringUtils.join(assignList.toArray(), ",") + "审批");
            process.setStatus(1);
        }else {
            if (approvalVo.getStatus() ==1){
                process.setDescription("审批通过（通过）");
                process.setStatus(2);
            }else {
                process.setDescription("审批通过（驳回）");
                process.setStatus(-1);
            }
        }
        //推送消息给申请人
        messageService.pushProcessedMessage(process.getId(), process.getUserId(), approvalVo.getStatus());
        processMapper.updateById(process);
    }

    @Override
    public IPage<ProcessVo> findProcessed(Page<java.lang.Process> pageParam) {
        // 根据当前人的ID查询
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery().taskAssignee(LoginUserInfoHelper.getUsername()).finished().orderByTaskCreateTime().desc();
        List<HistoricTaskInstance> list = query.listPage((int) ((pageParam.getCurrent() - 1) * pageParam.getSize()), (int) pageParam.getSize());
        long totalCount = query.count();

        List<ProcessVo> processList = new ArrayList<>();
        for (HistoricTaskInstance item : list) {
            String processInstanceId = item.getProcessInstanceId();
            Process process = this.getOne(new LambdaQueryWrapper<Process>().eq(Process::getProcessInstanceId, processInstanceId));
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process, processVo);
            processVo.setTaskId("0");
            processList.add(processVo);
        }
        IPage<ProcessVo> page = new Page<ProcessVo>(pageParam.getCurrent(), pageParam.getSize(), totalCount);
        page.setRecords(processList);
        return page;
    }

    @Override
    public IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam) {
        ProcessQueryVo processQueryVo = new ProcessQueryVo();
        processQueryVo.setUserId(LoginUserInfoHelper.getUserId());
        IPage<ProcessVo> page = processMapper.selectPage(pageParam, processQueryVo);
        for (ProcessVo item : page.getRecords()) {
            item.setTaskId("0");
        }
        return page;
    }

    private void endTask(String taskId) {
        //  当前任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        List endEventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        // 并行任务可能为null
        if(CollectionUtils.isEmpty(endEventList)) {
            return;
        }
        FlowNode endFlowNode = (FlowNode) endEventList.get(0);
        FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());

        //  临时保存当前活动的原始方向
        List originalSequenceFlowList = new ArrayList<>();
        originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());
        //  清理活动方向
        currentFlowNode.getOutgoingFlows().clear();

        //  建立新方向
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId("newSequenceFlowId");
        newSequenceFlow.setSourceFlowElement(currentFlowNode);
        newSequenceFlow.setTargetFlowElement(endFlowNode);
        List newSequenceFlowList = new ArrayList<>();
        newSequenceFlowList.add(newSequenceFlow);
        //  当前节点指向新的方向
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);

        //  完成当前任务
        taskService.complete(task.getId());

    }

    private List<Task> getCurrentTaskList(String id) {
        List<org.activiti.engine.task.Task> taskList = taskService.createTaskQuery().processInstanceId(id).list();
        return taskList;
    }
}
