package com.xxl.job.core.biz.client;

import com.xxl.job.core.CustomAdminBiz;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.CustomTriggerParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.XxlJobRemotingUtil;

/**
 * @author jiangwangfa
 * @date 2025/1/14
 * @description
 */
public class CustomExecutorBiz extends AdminBizClient implements CustomAdminBiz {
    private String loginIdentityKey;

    public CustomExecutorBiz(){

    }
    public CustomExecutorBiz(String addressUrl, String accessToken, int timeout, String loginIdentityKey) {
        super(addressUrl, accessToken, timeout);
        this.loginIdentityKey = loginIdentityKey;
    }

    @Override
    public ReturnT<String> triggerWithJobHandler(CustomTriggerParam triggerParam) {
        return XxlJobRemotingUtil.postBody(addressUrl+"api/callback", accessToken, timeout, triggerParam, String.class);
    }
}
