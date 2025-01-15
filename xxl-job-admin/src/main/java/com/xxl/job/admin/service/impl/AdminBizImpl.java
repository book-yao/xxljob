package com.xxl.job.admin.service.impl;

import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.thread.JobCompleteHelper;
import com.xxl.job.admin.core.thread.JobRegistryHelper;
import com.xxl.job.admin.core.thread.JobTriggerPoolHelper;
import com.xxl.job.admin.core.trigger.TriggerTypeEnum;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.CustomTriggerParam;
import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author xuxueli 2017-07-27 21:54:20
 */
@Service
public class AdminBizImpl implements AdminBiz {
    @Resource
    private XxlJobGroupDao xxlJobGroupDao;
    @Resource
    private XxlJobInfoDao xxlJobInfoDao;

    @Override
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList) {
        return JobCompleteHelper.getInstance().callback(callbackParamList);
    }

    @Override
    public ReturnT<String> registry(RegistryParam registryParam) {
        return JobRegistryHelper.getInstance().registry(registryParam);
    }

    @Override
    public ReturnT<String> registryRemove(RegistryParam registryParam) {
        return JobRegistryHelper.getInstance().registryRemove(registryParam);
    }

    @Override
    public ReturnT<String> trigger(CustomTriggerParam triggerParam) {
        String executorParams = triggerParam.getExecutorParams();
        int jobId = triggerParam.getJobId();
        String appName = triggerParam.getAppName();
        String executorHandler = triggerParam.getExecutorHandler();
        String executorShardingParam = triggerParam.getExecutorShardingParam();
        if (jobId > 0) {
            XxlJobInfo xxlJobInfo = xxlJobInfoDao.loadById(jobId);
            if (xxlJobInfo == null) {
                return new ReturnT<>(ReturnT.FAIL.getCode(), I18nUtil.getString("jobinfo_glue_jobid_unvalid"));
            }
            JobTriggerPoolHelper.trigger(jobId, TriggerTypeEnum.API, -1, executorShardingParam, executorParams, triggerParam.getAddressList());
            return ReturnT.SUCCESS;
        }
        List<XxlJobGroup> all = xxlJobGroupDao.list(appName);
        if (CollectionUtils.isEmpty(all)) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "appName not found");
        }
        List<XxlJobInfo> xxlJobInfos = xxlJobInfoDao.listJobsByExecutorHandler(executorHandler);
        if (CollectionUtils.isEmpty(xxlJobInfos)) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "jobInfo not found");
        }
        XxlJobInfo xxlJobInfo = null;
        for (XxlJobGroup xxlJobGroup : all) {
            if (!Objects.equals(appName, xxlJobGroup.getAppname())) {
                continue;
            }
            for (XxlJobInfo job : xxlJobInfos) {
                if (Objects.equals(executorHandler, job.getExecutorHandler())) {
                    if (xxlJobInfo != null) {
                        return new ReturnT<>(ReturnT.FAIL_CODE, "jobInfo has more than one");
                    }
                    xxlJobInfo = job;
                }
            }
        }
        if (xxlJobInfo == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "jobInfo not found");
        }
        JobTriggerPoolHelper.trigger(xxlJobInfo.getId(), TriggerTypeEnum.API, -1, executorShardingParam, executorParams, triggerParam.getAddressList());
        return ReturnT.SUCCESS;
    }

}
