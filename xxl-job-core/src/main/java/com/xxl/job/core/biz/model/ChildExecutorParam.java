package com.xxl.job.core.biz.model;

import java.io.Serializable;

/**
 * @author jiangwangfa
 * @date 2025/1/13
 * @description
 */
public class ChildExecutorParam implements Serializable {
    private static final long serialVersionUID = 1024001L;
    /**
     * 是否应用参数
     */
    private boolean paramApply;
    /**
     * 是否不执行子任务
     */
    private boolean nonExecChildJob;
    private String param;

    public ChildExecutorParam() {
    }

    public static ChildExecutorParam nonExecChildJob() {
        ChildExecutorParam childExecutorParam = new ChildExecutorParam();
        childExecutorParam.setNonExecChildJob(true);
        return childExecutorParam;
    }

    public ChildExecutorParam(String param) {
        this.paramApply = true;
        this.param = param;
    }

    public boolean isParamApply() {
        return paramApply;
    }

    public void setParamApply(boolean paramApply) {
        this.paramApply = paramApply;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public boolean isNonExecChildJob() {
        return nonExecChildJob;
    }

    public void setNonExecChildJob(boolean nonExecChildJob) {
        this.nonExecChildJob = nonExecChildJob;
    }
}
