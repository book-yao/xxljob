package com.xxl.job.core.biz.client;

import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.*;
import com.xxl.job.core.util.XxlJobRemotingUtil;

import java.util.List;
import java.util.Objects;

/**
 * admin api test
 *
 * @author xuxueli 2017-07-28 22:14:52
 */
public class AdminBizClient implements AdminBiz {

    public AdminBizClient() {
    }
    public AdminBizClient(String addressUrl, String accessToken, int timeout) {
        this.addressUrl = addressUrl;
        this.accessToken = accessToken;
        this.timeout = timeout;

        // valid
        if (!this.addressUrl.endsWith("/")) {
            this.addressUrl = this.addressUrl + "/";
        }
        if (!(this.timeout >=1 && this.timeout <= 10)) {
            this.timeout = 3;
        }
    }

    String addressUrl ;
    String accessToken;
    int timeout;

    public String getAddressUrl() {
        return addressUrl;
    }

    @Override
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList) {
        return XxlJobRemotingUtil.postBody(addressUrl+"api/callback", accessToken, timeout, callbackParamList, String.class);
    }

    @Override
    public ReturnT<String> registry(RegistryParam registryParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + "api/registry", accessToken, timeout, registryParam, String.class);
    }

    @Override
    public ReturnT<String> registryRemove(RegistryParam registryParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + "api/registryRemove", accessToken, timeout, registryParam, String.class);
    }

    @Override
    public ReturnT<String> trigger(CustomTriggerParam triggerParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + "api/trigger", accessToken, timeout, triggerParam, String.class);
    }

    @Override
    public ReturnT<String> logExeInfo(LogParam logParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + "api/logExeInfo", accessToken, timeout, logParam, String.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminBizClient that = (AdminBizClient) o;
        return timeout == that.timeout && Objects.equals(addressUrl, that.addressUrl) && Objects.equals(accessToken, that.accessToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressUrl, accessToken, timeout);
    }
}
