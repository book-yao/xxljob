package com.xxl.job.core.biz.model;

/**
 * @author jiangwangfa
 * @date 2025/1/13
 * @description
 */
public class ChildExecutorParam {
    private boolean apply;
    private boolean execChildJob = true;
    private String param;

    public ChildExecutorParam() {
    }

    public ChildExecutorParam(boolean apply, String param) {
        this.apply = apply;
        this.param = param;
    }

    public boolean isApply() {
        return apply;
    }

    public void setApply(boolean apply) {
        this.apply = apply;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public boolean isExecChildJob() {
        return execChildJob;
    }

    public void setExecChildJob(boolean execChildJob) {
        this.execChildJob = execChildJob;
    }
}
