package com.xxl.job.core;

import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.CustomTriggerParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.ReturnT;

/**
 * @author jiangwangfa
 * @date 2025/1/14
 * @description
 */
public interface CustomAdminBiz extends AdminBiz {

     ReturnT<String> triggerWithJobHandler(CustomTriggerParam triggerParam);
}
