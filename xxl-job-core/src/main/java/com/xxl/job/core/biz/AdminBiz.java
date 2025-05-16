package com.xxl.job.core.biz;

import com.xxl.job.core.biz.model.*;

import java.util.List;

/**
 * @author xuxueli 2017-07-27 21:52:49
 */
public interface AdminBiz {


    // ---------------------- callback ----------------------

    /**
     * callback
     *
     * @param callbackParamList
     * @return
     */
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList);


    // ---------------------- registry ----------------------

    /**
     * registry
     *
     * @param registryParam
     * @return
     */
    public ReturnT<String> registry(RegistryParam registryParam);

    /**
     * registry remove
     *
     * @param registryParam
     * @return
     */
    public ReturnT<String> registryRemove(RegistryParam registryParam);

    /**
     * trigger
     * @param triggerParam
     * @return
     */
    ReturnT<String> trigger(CustomTriggerParam triggerParam);

    /**
     * 执行日志结果
     *
     * @param logParam
     * @return
     */
    ReturnT<String> logExeInfo(LogParam logParam);
    // ---------------------- biz (custome) ----------------------
    // group、job ... manage

}
