package com.xiaojin.process.service;

import com.atguigu.model.process.ProcessTemplate;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 审批模板 服务类
 * </p>
 *
 * @author xiaojin
 * @since 2023-07-17
 */
public interface OaProcessTemplateService extends IService<ProcessTemplate> {

    Page<ProcessTemplate> selectPage(Page<ProcessTemplate> pageParam);

    void publish(Long id);
}
