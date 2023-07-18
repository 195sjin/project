package com.xiaojin.process.service;

import com.atguigu.model.process.ProcessType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author xiaojin
 * @since 2023-07-17
 */
public interface OaProcessTypeService extends IService<ProcessType> {

    List<ProcessType> findProcessType();

}
